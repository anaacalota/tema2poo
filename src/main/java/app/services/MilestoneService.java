package app.services;

import app.AppContext;
import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.JsonNode;
import entities.milestones.Milestone;
import entities.tickets.HistoryEntry;
import entities.tickets.Ticket;
import entities.tickets.enums.Action;
import entities.tickets.enums.Priority;
import entities.tickets.enums.Status;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.users.User;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MilestoneService {
    private static final double PERCENT_MULTIPLIER = 100.0;
    private static final int PRIORITY_CHANGE_DAYS = 3;

    private final AppContext context;
    private final FlowService flowService;

    public MilestoneService(final FlowService flowService) {
        this.context = AppContext.getInstance();
        this.flowService = flowService;
    }

    /**
     * Validates and creates a new milestone
     *
     * @param username the manager creating the milestone
     * @param timestamp current command timestamp
     * @param name name for the milestone
     * @param dueDate the deadline
     * @param params JSON node containing ticket IDs, assigned devs, and blocked milestones
     * @return an error ObjectNode if validation fails, or null on successful creation
     */
    public ObjectNode createMilestone(final String username, final String timestamp,
                                      final String name, final String dueDate,
                                      final JsonNode params) {
        ObjectNode result = ResultUtils.createBaseNode("createMilestone", username, timestamp);

        // milestones cannot be created during testing phase
        if (flowService.isTestingPhase(timestamp)) {
            result.put("error", "Milestones cannot be created during testing phase.");
            return result;
        }

        // check if milestone already exists
        if (context.milestoneExists(name)) {
            result.put("error", "Milestone " + name + " already exists.");
            return result;
        }

        // extract fields
        List<Integer> ticketIds = extractTicketIds(params);
        List<String> assignedDevs = extractAssignedDevs(params);
        List<String> blockingFor = extractBlockingFor(params);

        // check if tickets already belong to another milestone
        for (int id : ticketIds) {
            String existing = context.getTicketMilestoneName(id);
            if (existing != null) {
                result.put("error", "Tickets " + id + " already assigned to milestone "
                        + existing + ".");
                return result;
            }
        }

        // check that all tickets exist
        for (int id : ticketIds) {
            if (!context.ticketExists(id)) {
                result.put("error", "Ticket " + id + " does not exist.");
                return result;
            }
        }

        // create milestone
        Milestone milestone = new Milestone(name, dueDate, ticketIds,
                assignedDevs, blockingFor, username, timestamp);

        context.addMilestone(milestone);

        // make developers observers of milestone
        for (String devName : assignedDevs) {
            User user = context.getUser(devName);
            milestone.addObserver(user);
        }
        milestone.notifyCreated();

        // add history entry for each ticket added to this milestone
        for (int id : ticketIds) {
            Ticket ticket = context.getTicket(id);
            if (ticket != null) {
                HistoryEntry entry = new HistoryEntry(Action.ADDED_TO_MILESTONE,
                        username, LocalDate.parse(timestamp));
                entry.setMilestoneName(name);
                ticket.addHistoryEntry(entry);
            }
        }

        return null;
    }

    /**
     * Retrieves and filters milestones based on the user's role and assignments
     *
     * @param username the user requesting the list
     * @param timestamp current command timestamp
     * @return an ObjectNode containing a sorted list of visible milestones
     */
    public ObjectNode viewMilestones(final String username, final String timestamp) {
        ObjectNode result = ResultUtils.createBaseNode("viewMilestones", username, timestamp);
        ArrayNode array = ResultUtils.MAPPER.createArrayNode();

        LocalDate now = LocalDate.parse(timestamp);

        // select milestones based on role
        List<Milestone> milestones = new ArrayList<>();
        String role = context.getUser(username).getRole().name();

        if (role.equals("MANAGER")) {
            for (Milestone milestone : context.getAllMilestones()) {
                if (milestone.getCreatedBy().equals(username)) {
                    milestones.add(milestone);
                }
            }
        } else { // developer
            for (Milestone milestone : context.getAllMilestones()) {
                if (milestone.getAssignedDevs().contains(username)) {
                    milestones.add(milestone);
                }
            }
        }

        // sort by dueDate, then name
        milestones.sort(Comparator.comparing(Milestone::getDueDate)
                .thenComparing(Milestone::getName));

        // format each milestone
        for (Milestone milestone : milestones) {
            array.add(formatMilestone(milestone, now));
        }

        result.set("milestones", array);
        return result;
    }

    /**
     * Formats a milestone
     *
     * @param milestone the milestone to format
     * @param now the current date used for time-based calculations
     * @return a populated ObjectNode with milestone details
     */
    private ObjectNode formatMilestone(final Milestone milestone, final LocalDate now) {
        ObjectNode node = ResultUtils.MAPPER.createObjectNode();
        node.put("name", milestone.getName());
        node.put("dueDate", milestone.getDueDate().toString());
        node.put("createdAt", milestone.getCreatedAt().toString());
        node.put("createdBy", milestone.getCreatedBy());
        node.set("blockingFor", ResultUtils.MAPPER.valueToTree(milestone.getBlockingFor()));
        node.set("tickets", ResultUtils.MAPPER.valueToTree(milestone.getTickets()));
        node.set("assignedDevs", ResultUtils.MAPPER.valueToTree(milestone.getAssignedDevs()));

        if (milestone.isActive()) {
            node.put("status", "ACTIVE");
        } else {
            node.put("status", "COMPLETED");
        }

        node.put("isBlocked", context.isMilestoneBlocked(milestone.getName()));

        LocalDate completionDate = completionDate(milestone);
        LocalDate referenceDate = completionDate == null ? now : completionDate;
        long daysUntilDue = Math.max(0, ChronoUnit.DAYS.between(referenceDate,
                milestone.getDueDate()) + 1);
        long overdueBy = Math.max(0, ChronoUnit.DAYS.between(milestone.getDueDate(),
                referenceDate) + 1);
        if (completionDate == null && !now.isAfter(milestone.getDueDate())) {
            overdueBy = 0;
        }
        node.put("daysUntilDue", daysUntilDue);
        node.put("overdueBy", overdueBy);

        // open / closed tickets
        List<Integer> open = new ArrayList<>();
        List<Integer> closed = new ArrayList<>();

        for (int id : milestone.getTickets()) {
            Ticket ticket = context.getTicket(id);
            if (ticket != null) {
                if (ticket.getStatus() == Status.CLOSED) {
                    closed.add(id);
                } else {
                    open.add(id);
                }
            }
        }

        node.set("openTickets", ResultUtils.MAPPER.valueToTree(open));
        node.set("closedTickets", ResultUtils.MAPPER.valueToTree(closed));

        // completion percentage
        double completion = 0.0;
        if (!milestone.getTickets().isEmpty()) {
            completion = (double) closed.size() / milestone.getTickets().size();
            completion = Math.round(completion * PERCENT_MULTIPLIER) / PERCENT_MULTIPLIER;
        }
        node.put("completionPercentage", completion);
        node.set("repartition", computeRepartition(milestone));

        return node;
    }

    /**
     * Calculates the ticket distribution among developers assigned to the milestone
     *
     * @param milestone the milestone
     * @return an ArrayNode containing developer names and their assigned tickets
     */
    private LocalDate completionDate(final Milestone milestone) {
        if (milestone.isActive()) {
            return null;
        }
        LocalDate latest = null;
        for (int id : milestone.getTickets()) {
            Ticket ticket = context.getTicket(id);
            if (ticket == null) {
                continue;
            }
            for (HistoryEntry entry : ticket.getHistory()) {
                if (entry.getToStatus() == Status.CLOSED
                        && (latest == null || entry.getTimestamp().isAfter(latest))) {
                    latest = entry.getTimestamp();
                }
            }
        }
        return latest;
    }

    private ArrayNode computeRepartition(final Milestone milestone) {
        ArrayNode array = ResultUtils.MAPPER.createArrayNode();
        Map<String, List<Integer>> map = new HashMap<>();

        for (String dev : milestone.getAssignedDevs()) {
            map.put(dev, new ArrayList<>());
        }

        // group tickets by assigned developer
        for (int id : milestone.getTickets()) {
            Ticket ticket = context.getTicket(id);
            if (ticket != null) {
                String dev = ticket.getAssignedTo();
                if (dev != null && map.containsKey(dev)) {
                    map.get(dev).add(id);
                }
            }
        }

        // sort by ticket count, then name
        List<Map.Entry<String, List<Integer>>> entries = new ArrayList<>(map.entrySet());
        entries.sort(Comparator.comparingInt((Map.Entry<String, List<Integer>> entry)
                -> entry.getValue().size()).thenComparing(Map.Entry::getKey));

        for (Map.Entry<String, List<Integer>> entry : entries) {
            ObjectNode devNode = ResultUtils.MAPPER.createObjectNode();
            devNode.put("developer", entry.getKey());
            devNode.set("assignedTickets", ResultUtils.MAPPER.valueToTree(entry.getValue()));
            array.add(devNode);
        }

        return array;
    }

    /**
     * Parses a list of ticket IDs from a JSON node
     *
     * @param params the JSON parameters of the command
     * @return a list of ticket identifiers
     */
    private List<Integer> extractTicketIds(final JsonNode params) {
        List<Integer> list = new ArrayList<>();
        if (params.has("tickets")) {
            for (JsonNode node : params.get("tickets")) {
                list.add(node.asInt());
            }
        }
        return list;
    }

    /**
     * Parses a list of assigned developer usernames from a JSON node
     *
     * @param params the JSON parameters of the command
     * @return a list of developer usernames
     */
    private List<String> extractAssignedDevs(final JsonNode params) {
        List<String> list = new ArrayList<>();
        if (params.has("assignedDevs")) {
            for (JsonNode node : params.get("assignedDevs")) {
                list.add(node.asText());
            }
        }
        return list;
    }

    /**
     * Parses a list of milestone names that are blocked by this milestone
     *
     * @param params the JSON parameters of the command
     * @return a list of blocked milestone names
     */
    private List<String> extractBlockingFor(final JsonNode params) {
        List<String> list = new ArrayList<>();
        if (params.has("blockingFor")) {
            for (JsonNode node : params.get("blockingFor")) {
                list.add(node.asText());
            }
        }
        return list;
    }

    /**
     * Updates the priority of tickets in a milestone based on their creation date
     *
     * @param milestone the milestone to update
     * @param now the current date
     */
    public void updateMilestonePriorities(final Milestone milestone, final LocalDate now) {
        if (context.isMilestoneBlocked(milestone.getName())) {
            milestone.markBlocked();
            return;
        }

        LocalDate created = milestone.getCreatedAt();
        long daysPassed = ChronoUnit.DAYS.between(created, now);

        // every 3 days, change the priority of tickets in this milestone
        int currentLevel = (int) (daysPassed / PRIORITY_CHANGE_DAYS);

        if (milestone.wasPreviouslyBlocked()
                && !milestone.wasPriorityResetAfterUnblock()) {
            milestone.setLastChangeLevel(currentLevel);
            milestone.markPriorityResetAfterUnblock();
            return;
        }

        // check if we already changed the priority
        if (currentLevel > milestone.getLastChangeLevel()) {
            int stepsToIncrease = currentLevel - milestone.getLastChangeLevel();
            for (int ticketId : milestone.getTickets()) {
                Ticket ticket = context.getTicket(ticketId);
                if (ticket == null || ticket.getStatus() == Status.CLOSED) {
                    continue;
                }

                // upgrade priority based on the number of days since last change
                for (int step = 0; step < stepsToIncrease; step++) {
                    upgradePriority(ticket);
                }
                flowService.reopenIfDeveloperCannotHandle(ticket, now);
            }
            milestone.setLastChangeLevel(currentLevel);
        }
    }

    /**
     * Upgrades the priority of a ticket based on its current level
     *
     * @param ticket the ticket to upgrade
     */
    private void upgradePriority(final Ticket ticket) {
        switch (ticket.getBusinessPriority()) {
            case LOW -> ticket.setBusinessPriority(Priority.MEDIUM);
            case MEDIUM -> ticket.setBusinessPriority(Priority.HIGH);
            case HIGH -> ticket.setBusinessPriority(Priority.CRITICAL);
            default -> {

            }
        }
    }

    /**
     * Updates the priority of tickets to CRITICAL the day before their due date
     *
     * @param milestone the milestone to update
     * @param now the current date
     */
    public void updateCriticalBeforeDue(final Milestone milestone, final LocalDate now) {
        if (context.isMilestoneBlocked(milestone.getName())) {
            return;
        }

        LocalDate due = milestone.getDueDate();
        LocalDate dayBefore = due.minusDays(1);

        if (!now.equals(dayBefore)) {
            return;
        }
        for (int id : milestone.getTickets()) {
            Ticket ticket = context.getTicket(id);
            if (ticket != null && ticket.getStatus() != Status.CLOSED) {
                ticket.setBusinessPriority(Priority.CRITICAL);
                flowService.reopenIfDeveloperCannotHandle(ticket, now);
            }
        }
    }
}
