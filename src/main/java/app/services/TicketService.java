package app.services;

import app.AppContext;
import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import entities.comments.Comment;
import entities.milestones.Milestone;
import entities.tickets.Ticket;
import entities.tickets.TicketFactory;
import entities.tickets.enums.Action;
import entities.tickets.HistoryEntry;
import entities.tickets.enums.Status;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.users.Developer;
import entities.users.User;
import entities.users.enums.UserRole;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TicketService {
    private static final int MAX_CHARACTERS = 10;
    private final AppContext context;
    private final FlowService flowService;
    private final ObjectMapper mapper = ResultUtils.MAPPER;

    public TicketService(final FlowService flowService) {
        this.context = AppContext.getInstance();
        this.flowService = flowService;
    }

    /**
     * Creates a new ticket
     *
     * @param username username of the reporter
     * @param timestamp timestamp of the report
     * @param params ticket parameters
     * @return ObjectNode with the ticket id
     */
    public ObjectNode reportTicket(final String username, final String timestamp,
                                   final JsonNode params) {
        ObjectNode result = ResultUtils.createBaseNode("reportTicket", username, timestamp);

        if (!flowService.isTestingPhase(timestamp)) {
            result.put("error", "Tickets can only be reported during testing phases.");
            return result;
        }

        String type = params.get("type").asText();
        String reportedBy;

        if (params.has("reportedBy") && !params.get("reportedBy").isNull()) {
            reportedBy = params.get("reportedBy").asText();
        } else {
            reportedBy = "";
        }

        if (reportedBy.isEmpty() && !type.equals("BUG")) {
            result.put("error", "Anonymous reports are only allowed for tickets of type BUG.");
            return result;
        }

        ObjectNode processed = (ObjectNode) params;
        if (reportedBy.isEmpty()) {
            processed.put("businessPriority", "LOW");
        }
        processed.put("status", "OPEN");

        int id = context.nextTicketId();
        Ticket ticket = TicketFactory.createTicket(id, params);

        ticket.setReportedBy(reportedBy);
        ticket.setStatus(Status.OPEN);
        ticket.setCreatedAt(LocalDate.parse(timestamp));

        User reporter = context.getUser(username);
        ticket.addObserver(reporter);
        context.addTicket(ticket);

        return null;
    }

    /**
     * Assigns a ticket to a developer
     *
     * @param dev username of the developer
     * @param timestamp timestamp of the action
     * @param ticketId id of the ticket to be assigned
     * @return ObjectNode with the ticket id
     */
    public ObjectNode assignTicket(final String dev, final String timestamp, final int ticketId) {
        ObjectNode result = ResultUtils.createBaseNode("assignTicket", dev, timestamp);

        Ticket ticket = context.getTicket(ticketId);
        if (ticket == null) {
            result.put("error", "Ticket " + ticketId + " does not exist.");
            return result;
        }

        if (ticket.getStatus() != Status.OPEN) {
            result.put("error", "Only OPEN tickets can be assigned.");
            return result;
        }

        User user = context.getUser(dev);
        if (!(user.isDeveloper())) {
            result.put("error", "Only developers can assign tickets.");
            return result;
        }
        Developer developer = (Developer) user;

        // expertise check
        if (!developer.canHandleExpertiseArea(ticket.getExpertiseArea())) {
            StringBuilder requiredAreas = getRequiredAreas(ticket);

            result.put("error", "Developer " + dev + " cannot assign ticket " + ticketId
                    + " due to expertise area. Required: " + requiredAreas
                    + "; Current: " + developer.getExpertiseArea() + ".");
            return result;
        }

        // seniority check
        if (!developer.canHandleTicket(ticket.getType(), ticket.getBusinessPriority())) {
            StringBuilder required = new StringBuilder();
            for (int seniorityIndex = 0; seniorityIndex < ticket.getRequiredSenioritiesSorted()
                                                                .size(); seniorityIndex++) {
                required.append(ticket.getRequiredSenioritiesSorted().get(seniorityIndex).name());
                if (seniorityIndex < ticket.getRequiredSenioritiesSorted().size() - 1) {
                    required.append(", ");
                }
            }

            result.put("error", "Developer " + dev + " cannot assign ticket " + ticketId
                    + " due to seniority level. Required: " + required
                    + "; Current: " + developer.getSeniority() + ".");

            ticket.addObserver(user);
            return result;
        }

        String milestoneName = context.getTicketMilestoneName(ticketId);
        if (milestoneName != null) {
            Milestone milestone = context.getMilestone(milestoneName);
            // must be assigned to milestone
            if (!milestone.getAssignedDevs().contains(dev)) {
                result.put("error", "Developer " + dev
                        + " is not assigned to milestone " + milestoneName + ".");
                return result;
            }

            // milestone must not be blocked
            if (context.isMilestoneBlocked(milestoneName)) {
                result.put("error", "Cannot assign ticket " + ticketId
                        + " from blocked milestone " + milestoneName + ".");
                return result;
            }
        }

        // set the data for assigned
        ticket.setAssignedTo(dev);
        ticket.setAssignedAt(LocalDate.parse(timestamp));
        ticket.setStatus(Status.IN_PROGRESS);

        // add entry for a new assignment
        HistoryEntry firstEntry = new HistoryEntry(Action.ASSIGNED,
                dev, LocalDate.parse(timestamp));
        ticket.addHistoryEntry(firstEntry);

        // add entry for status change
        HistoryEntry secondEntry = new HistoryEntry(Action.STATUS_CHANGED, dev,
                LocalDate.parse(timestamp));
        secondEntry.setFromStatus(Status.OPEN);
        secondEntry.setToStatus(Status.IN_PROGRESS);
        ticket.addHistoryEntry(secondEntry);

        return null;
    }

    /**
     * Helper method to get required expertise areas as a string
     *
     * @param ticket the ticket to process
     * @return a StringBuilder containing the required expertise areas
     */
    private static StringBuilder getRequiredAreas(final Ticket ticket) {
        StringBuilder requiredAreas = new StringBuilder();
        for (int expertiseIndex = 0; expertiseIndex
                < ticket.getRequiredExpertiseAreasSorted().size(); expertiseIndex++) {
            requiredAreas.append(ticket.getRequiredExpertiseAreasSorted()
                                        .get(expertiseIndex).name());
            if (expertiseIndex < ticket.getRequiredExpertiseAreasSorted().size() - 1) {
                requiredAreas.append(", ");
            }
        }
        return requiredAreas;
    }

    /**
     * Unassigns a ticket from a developer
     *
     * @param dev username of the developer
     * @param timestamp timestamp of the action
     * @param ticketId id of the ticket to be unassigned
     * @return ObjectNode with the ticket id
     */
    public ObjectNode undoAssignTicket(final String dev, final String timestamp,
                                       final int ticketId) {
        ObjectNode result = ResultUtils.createBaseNode("undoAssignTicket", dev, timestamp);
        Ticket ticket = context.getTicket(ticketId);

        if (ticket == null) {
            result.put("error", "Ticket " + ticketId + " does not exist.");
            return result;
        }

        if (ticket.getStatus() != Status.IN_PROGRESS) {
            result.put("error", "Only IN_PROGRESS tickets can be unassigned.");
            return result;
        }

        if (!dev.equals(ticket.getAssignedTo())) {
            result.put("error",
                    "Developer " + dev + " cannot unassign ticket " + ticketId + ".");
            return result;
        }

        ticket.removeObserver(context.getUser(dev));

        // undo
        ticket.setAssignedTo(null);
        ticket.setAssignedAt(null);
        ticket.setStatus(Status.OPEN);

        HistoryEntry entry = new HistoryEntry(Action.DE_ASSIGNED, dev,
                LocalDate.parse(timestamp));
        ticket.addHistoryEntry(entry);

        return null;
    }

    /**
     * Views all tickets assigned to a developer
     *
     * @param dev username of the developer
     * @param timestamp timestamp of the action
     * @return ArrayNode with the tickets
     */
    public ObjectNode viewAssignedTickets(final String dev, final String timestamp) {
        ObjectNode result = ResultUtils.createBaseNode("viewAssignedTickets", dev, timestamp);
        ArrayNode array = mapper.createArrayNode();

        List<Ticket> assigned = context.getAllTickets().stream()
                .filter(ticket -> dev.equals(ticket.getAssignedTo()))
                .sorted(Comparator.comparing(Ticket::getBusinessPriority).reversed()
                                  .thenComparing(Ticket::getCreatedAt)
                                  .thenComparing(Ticket::getId))
                .toList();

        for (Ticket ticket : assigned) {
            array.add(ticketToJsonAssigned(ticket));
        }

        result.set("assignedTickets", array);
        return result;
    }

    /**
     * Gets all tickets for a manager
     *
     * @return a list of tickets
     */
    public List<Ticket> ticketsForManager() {
        return new ArrayList<>(context.getAllTickets());
    }

    /**
     * Gets all tickets for a reporter
     *
     * @param username username of the reporter
     * @return a list of tickets
     */
    public List<Ticket> ticketsForReporter(final String username) {
        List<Ticket> result = new ArrayList<>();

        for (Ticket ticket : context.getAllTickets()) {
            String reporter = ticket.getReportedBy();
            if (reporter != null && !reporter.isEmpty() && reporter.equals(username)) {
                result.add(ticket);
            }
        }

        return result;
    }

    /**
     * Gets all tickets for a developer
     *
     * @param username username of the developer
     * @return a list of tickets
     */
    public List<Ticket> ticketsForDeveloper(final String username) {
        List<Ticket> result = new ArrayList<>();

        for (Milestone milestone : context.getAllMilestones()) {
            if (!milestone.getAssignedDevs().contains(username)) {
                continue;
            }
            if (context.isMilestoneBlocked(milestone.getName())) {
                continue;
            }

            for (int id : milestone.getTickets()) {
                Ticket ticket = context.getTicket(id);
                if (ticket.getStatus() == Status.OPEN) {
                    result.add(ticket);
                }
            }
        }

        return result;
    }

    /**
     * Adds a comment to a ticket
     *
     * @param username username of the user
     * @param timestamp timestamp of the action
     * @param ticketId id of the ticket to be commented on
     * @param content comment content
     * @return ObjectNode with the ticket id
     */
    public ObjectNode addComment(final String username, final String timestamp,
                                 final int ticketId, final String content) {
        ObjectNode result = ResultUtils.createBaseNode("addComment", username, timestamp);
        Ticket ticket = context.getTicket(ticketId);

        if (ticket == null) {
            return null;
        }

        if (ticket.getReportedBy() == null || ticket.getReportedBy().isEmpty()) {
            result.put("error", "Comments are not allowed on anonymous tickets.");
            return result;
        }

        User user = context.getUser(username);

        // reporter cannot comment on closed tickets
        if (user.getRole() == UserRole.REPORTER && ticket.getStatus() == Status.CLOSED) {
            result.put("error", "Reporters cannot comment on CLOSED tickets.");
            return result;
        }

        // comment must be at least 10 characters
        if (content == null || content.trim().length() < MAX_CHARACTERS) {
            result.put("error", "Comment must be at least 10 characters long.");
            return result;
        }

        // developer can comment only on tickets assigned to them
        if (user.getRole() == UserRole.DEVELOPER) {
            if (!username.equals(ticket.getAssignedTo())) {
                result.put("error", "Ticket " + ticketId
                        + " is not assigned to the developer " + username + ".");
                return result;
            }
        }

        // reporter can comment only on tickets they reported
        if (user.getRole() == UserRole.REPORTER) {
            if (!username.equals(ticket.getReportedBy())) {
                result.put("error", "Reporter " + username
                        + " cannot comment on ticket " + ticketId + ".");
                return result;
            }
        }

        // add comment
        Comment comment = new Comment(username, content, LocalDate.parse(timestamp));
        ticket.getComments().add(comment);

        return null;
    }

    /**
     * Undoes the last comment added by a user
     *
     * @param username username of the user who added the comment
     * @param timestamp timestamp of the action
     * @param ticketId id of the ticket to be commented on
     * @return ObjectNode with the ticket id
     */
    public ObjectNode undoAddComment(final String username, final String timestamp,
                                     final int ticketId) {
        ObjectNode result = ResultUtils.createBaseNode("undoAddComment", username, timestamp);

        Ticket ticket = context.getTicket(ticketId);
        if (ticket == null) {
            return null;
        }

        // anonymous ticket cannot have comments removed
        if (ticket.getReportedBy() == null || ticket.getReportedBy().isEmpty()) {
            result.put("error", "Comments are not allowed on anonymous tickets.");
            return result;
        }

        List<Comment> comments = ticket.getComments();

        // find last comment by this user
        Comment last = null;
        for (int index = comments.size() - 1; index >= 0; index--) {
            if (comments.get(index).getAuthor().equals(username)) {
                last = comments.get(index);
                break;
            }
        }

        if (last == null) {
            return null;
        }
        comments.remove(last);
        return null;
    }

    /** It shows the ticket history for a user
     *
     * @param username username of the user
     * @param timestamp timestamp of the action
     * @return ObjectNode with the ticket history
     */
    public ObjectNode viewTicketHistory(final String username, final String timestamp) {
        ObjectNode result = ResultUtils.createBaseNode("viewTicketHistory", username, timestamp);
        User user = context.getUser(username);
        List<Ticket> tickets;

        if (user.getRole() == UserRole.DEVELOPER) {
            tickets = ticketsForDeveloperHistory(username);
        } else {
            tickets = ticketsForManagerHistory(username);
        }

        tickets.sort(Comparator
                .comparing(Ticket::getCreatedAt)
                .thenComparing(Ticket::getId));
        ArrayNode historyArray = mapper.createArrayNode();

        for (Ticket ticket : tickets) {
            historyArray.add(ticketHistoryJson(ticket, username));
        }

        result.set("ticketHistory", historyArray);
        return result;
    }

    /**
     * It gets the tickets assigned to a developer
     *
     * @param dev the developer username
     * @return a list of tickets from the developer history
     */
    private List<Ticket> ticketsForDeveloperHistory(final String dev) {
        List<Ticket> list = new ArrayList<>();

        for (Ticket ticket : context.getAllTickets()) {
            boolean assignedEver = ticket.getHistory().stream()
                    .anyMatch(entry -> entry.getAction() == Action.ASSIGNED
                            && entry.getBy().equals(dev));
            boolean deassignedEver = ticket.getHistory().stream()
                    .anyMatch(entry -> entry.getAction() == Action.DE_ASSIGNED
                            && entry.getBy().equals(dev));
            boolean statusChanged = ticket.getHistory().stream()
                    .anyMatch(entry -> entry.getBy().equals(dev));
            boolean commented = ticket.getComments().stream()
                    .anyMatch(comment -> comment.getAuthor().equals(dev));
            if (assignedEver || deassignedEver || statusChanged || commented) {
                list.add(ticket);
            }
        }

        return list;
    }

    /**
     * It gets the tickets assigned to a manager (all in the milestone he created)
     *
     * @param manager the manager username
     * @return a list of tickets from the manager history
     */
    private List<Ticket> ticketsForManagerHistory(final String manager) {
        List<Ticket> list = new ArrayList<>();

        for (Milestone milestone : context.getAllMilestones()) {
            if (!milestone.getCreatedBy().equals(manager)) {
                continue;
            }

            for (int id : milestone.getTickets()) {
                list.add(context.getTicket(id));
            }
        }

        return list;
    }

    /**
     * The JSON format for the history command
     *
     * @param ticket the ticket to process
     * @param viewer the username of the user viewing the log
     * @return an ObjectNode representing the ticket's filtered history
     */
    private ObjectNode ticketHistoryJson(final Ticket ticket, final String viewer) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", ticket.getId());
        node.put("title", ticket.getTitle());
        node.put("status", ticket.getStatus().name());

        // actions
        ArrayNode actionsArray = mapper.createArrayNode();
        List<HistoryEntry> actions = new ArrayList<>(ticket.getHistory());

        // if a developer was de-assigned from a ticket, he should not see its history
        LocalDate limit = null;
        for (HistoryEntry entry : actions) {
            if (entry.getAction() == Action.DE_ASSIGNED && entry.getBy().equals(viewer)) {
                limit = entry.getTimestamp();
                break;
            }
        }

        for (HistoryEntry entry : actions) {
            if (limit != null && entry.getTimestamp().isAfter(limit)) {
                break;
            }
            actionsArray.add(entry.toJson());
        }

        node.set("actions", actionsArray);

        ArrayNode commentsArray = mapper.createArrayNode();
        for (Comment comment : ticket.getComments()) {
            if (limit != null && comment.getCreatedAt().isAfter(limit)) {
                continue;
            }

            ObjectNode commentNode = mapper.createObjectNode();
            commentNode.put("author", comment.getAuthor());
            commentNode.put("createdAt", comment.getCreatedAt().toString());
            commentNode.put("content", comment.getContent());
            commentsArray.add(commentNode);
        }
        node.set("comments", commentsArray);

        return node;
    }

    /**
     * Converts a ticket into its default JSON representation
     *
     * @param ticket the ticket to convert
     * @return an ObjectNode containing full ticket details
     */
    public ObjectNode ticketToJson(final Ticket ticket) {
        ObjectNode node = baseTicketJson(ticket);
        addSolvedAt(node, ticket);
        addAssignmentInfo(node, ticket);
        addReportedBy(node, ticket);
        node.set("comments", commentsToJson(ticket.getComments()));

        return node;
    }

    /**
     * Formats a ticket for the specific assigned tickets view
     *
     * @param ticket the ticket to process
     * @return a JSON node with assignment specific details
     */
    public ObjectNode ticketToJsonAssigned(final Ticket ticket) {
        ObjectNode node = baseTicketJson(ticket);
        node.put("assignedAt", ticket.getAssignedAt().toString());
        addReportedBy(node, ticket);
        node.set("comments", commentsToJson(ticket.getComments()));

        return node;
    }

    /**
     * Formats a ticket for search result views
     *
     * @param ticket the ticket to process
     * @return a simplified JSON node suitable for search outputs
     */
    public ObjectNode ticketToJsonSearch(final Ticket ticket) {
        ObjectNode node = baseTicketJson(ticket);
        addSolvedAt(node, ticket);
        addReportedBy(node, ticket);

        return node;
    }

    /**
     * Creates a JSON node with fields common to all ticket outputs
     *
     * @param ticket the source ticket
     * @return an ObjectNode with shared attributes
     */
    private ObjectNode baseTicketJson(final Ticket ticket) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", ticket.getId());
        node.put("type", ticket.getType());
        node.put("title", ticket.getTitle());
        node.put("businessPriority", ticket.getBusinessPriority().name());
        node.put("status", ticket.getStatus().name());
        node.put("createdAt", ticket.getCreatedAt().toString());
        return node;
    }

    /**
     * Adds the resolution date to the JSON node if available
     *
     * @param node the target JSON node
     * @param ticket the source ticket
     */
    private void addSolvedAt(final ObjectNode node, final Ticket ticket) {
        if (ticket.getStatus() == Status.RESOLVED || ticket.getStatus() == Status.CLOSED) {
            if (ticket.getSolvedAt() != null) {
                node.put("solvedAt", ticket.getSolvedAt().toString());
            } else {
                node.put("solvedAt", "");
            }
        } else {
            node.put("solvedAt", "");
        }
    }

    /**
     * Adds assignment and timestamp data to the JSON node
     *
     * @param node the target JSON node
     * @param ticket the source ticket
     */
    private void addAssignmentInfo(final ObjectNode node, final Ticket ticket) {
        if (ticket.getStatus() == Status.IN_PROGRESS
                || ticket.getStatus() == Status.RESOLVED
                || ticket.getStatus() == Status.CLOSED) {

            if (ticket.getAssignedAt() != null) {
                node.put("assignedAt", ticket.getAssignedAt().toString());
            } else {
                node.put("assignedAt", "");
            }

            if (ticket.getAssignedTo() != null) {
                node.put("assignedTo", ticket.getAssignedTo());
            } else {
                node.put("assignedTo", "");
            }

        } else {
            node.put("assignedAt", "");
            node.put("assignedTo", "");
        }
    }

    /**
     * Adds the reporter's name to the JSON node
     *
     * @param node the target JSON node
     * @param ticket the source ticket
     */
    private void addReportedBy(final ObjectNode node, final Ticket ticket) {
        node.put("reportedBy", ticket.getReportedBy() != null ? ticket.getReportedBy() : "");
    }

    /**
     * Transforms a list of comment objects into a formatted JSON array
     *
     * @param comments the list of comments to format
     * @return an ArrayNode containing all formatted comments
     */
    private ArrayNode commentsToJson(final List<Comment> comments) {
        ArrayNode array = mapper.createArrayNode();
        for (Comment c : comments) {
            ObjectNode commentNode = mapper.createObjectNode();
            commentNode.put("author", c.getAuthor());
            commentNode.put("content", c.getContent());
            commentNode.put("createdAt", c.getCreatedAt().toString());
            array.add(commentNode);
        }
        return array;
    }
}
