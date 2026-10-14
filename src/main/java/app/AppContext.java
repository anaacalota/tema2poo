package app;

import entities.milestones.Milestone;
import entities.tickets.Ticket;
import entities.users.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppContext {
    private static AppContext instance;

    private AppContext() {

    }

    /**
     * Returns the singleton instance of the application context
     *
     * @return the unique instance of AppContext
     */
    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    @Setter
    @Getter
    private boolean lostInvestors = false;
    @Setter
    @Getter
    private boolean testingPhaseActive = false;
    @Setter
    @Getter
    private LocalDate testingPhaseStartDate = null;
    private int nextTicketId = 0;
    private final Map<String, User> users = new HashMap<>();
    private final Map<Integer, Ticket> tickets = new HashMap<>();
    private final Map<String, Milestone> milestones = new HashMap<>();
    private final Map<Integer, String> ticketToMilestone = new HashMap<>();
    private final Map<String, String> blockingRelations = new HashMap<>();
    private final Map<String, List<String>> notifications = new HashMap<>();

    /**
     * Adds a user to the system database
     *
     * @param user the user object to be stored
     */
    public void addUser(final User user) {
        users.put(user.getUsername(), user);
    }

    /**
     * Retrieves a user by their username
     *
     * @param username the username to search for
     * @return the User object, or null if not found
     */
    public User getUser(final String username) {
        return users.get(username);
    }

    /**
     * Returns all registered users in the system
     *
     * @return a collection of all stored users
     */
    public Collection<User> getAllUsers() {
        return users.values();
    }

    /**
     * Stores a ticket in the system database
     *
     * @param ticket the ticket object to be stored
     */
    public void addTicket(final Ticket ticket) {
        tickets.put(ticket.getId(), ticket);
    }

    /**
     * Retrieves a ticket by its ID
     *
     * @param id the ID of the ticket
     * @return the Ticket object, or null if not found
     */
    public Ticket getTicket(final int id) {
        return tickets.get(id);
    }

    /**
     * Checks if a ticket with the given ID exists in the system
     *
     * @param id the ID to check
     * @return true if the ticket exists
     */
    public boolean ticketExists(final int id) {
        return tickets.containsKey(id);
    }

    /**
     * Returns all tickets currently stored in the system
     *
     * @return a collection of all tickets
     */
    public Collection<Ticket> getAllTickets() {
        return tickets.values();
    }

    /**
     * Generates a new ticket ID and increments the internal counter
     *
     * @return the next ticket ID
     */
    public int nextTicketId() {
        return nextTicketId++;
    }

    /**
     * Adds a milestone and updates indices for tickets and blocking relations
     *
     * @param milestone the milestone object to be added
     */
    public void addMilestone(final Milestone milestone) {
        milestones.put(milestone.getName(), milestone);

        for (int ticketId : milestone.getTickets()) {
            ticketToMilestone.put(ticketId, milestone.getName());
        }

        for (String blocked : milestone.getBlockingFor()) {
            blockingRelations.put(blocked, milestone.getName());
        }
    }

    /**
     * Retrieves a milestone by its name
     *
     * @param name the name of the milestone
     * @return the Milestone object, or null if not found
     */
    public Milestone getMilestone(final String name) {
        return milestones.get(name);
    }

    /**
     * Checks if a milestone with the given name exists in the system
     *
     * @param name the name to check
     * @return true if the milestone exists
     */
    public boolean milestoneExists(final String name) {
        return milestones.containsKey(name);
    }

    /**
     * Returns all milestones registered in the system
     *
     * @return a collection of all milestones
     */
    public Collection<Milestone> getAllMilestones() {
        return milestones.values();
    }

    /**
     * Returns the name of the milestone a specific ticket belongs to
     *
     * @param ticketId the ID of the ticket
     * @return the name of the parent milestone, or null if none
     */
    public String getTicketMilestoneName(final int ticketId) {
        return ticketToMilestone.get(ticketId);
    }

    /**
     * Determines if a milestone is currently blocked by another active milestone
     *
     * @param name the name of the milestone to check
     * @return true if the milestone is blocked by an existing active milestone
     */
    public boolean isMilestoneBlocked(final String name) {
        if (!blockingRelations.containsKey(name)) {
            return false;
        }

        String blockingMilestoneName = blockingRelations.get(name);
        Milestone blocking = milestones.get(blockingMilestoneName);

        if (blocking == null) {
            return false;
        }

        if (blocking.isActive()) {
            milestones.get(name).markBlocked();
            return true;
        }

        return blocking.isActive();
    }

    /** Notifies dependent milestones when a blocker becomes complete. */
    public void notifyMilestonesUnblockedByTicket(final int ticketId, final LocalDate date) {
        String sourceName = ticketToMilestone.get(ticketId);
        if (sourceName == null) {
            return;
        }
        Milestone source = milestones.get(sourceName);
        if (source == null || source.isActive()) {
            return;
        }
        for (String dependentName : source.getBlockingFor()) {
            Milestone dependent = milestones.get(dependentName);
            if (dependent != null && dependent.wasPreviouslyBlocked()
                    && !date.isAfter(dependent.getDueDate())
                    && !isMilestoneBlocked(dependentName)) {
                dependent.notifyUnblockedByTicket(ticketId);
                dependent.markUnblockedHandled();
            }
        }
    }

    /**
     * Stores a new notification message for a specific user
     *
     * @param username the recipient of the notification
     * @param message  the notification content
     */
    public void addNotification(final String username, final String message) {
        notifications.computeIfAbsent(username, _ -> new ArrayList<>()).add(message);
    }

    /**
     * Retrieves the list of notifications for a specific user
     *
     * @param username the username for which to get notifications
     * @return a list of notification strings
     */
    public List<String> getNotifications(final String username) {
        if (!notifications.containsKey(username)) {
            return List.of();
        }

        return new ArrayList<>(notifications.get(username));
    }

    /**
     * Removes all stored notifications for a specific user
     *
     * @param username the username to clear notifications for
     */
    public void clearNotifications(final String username) {
        notifications.remove(username);
    }

    /**
     * Resets the entire application context state to its initial values
     */
    public void reset() {
        users.clear();
        tickets.clear();
        milestones.clear();
        ticketToMilestone.clear();
        notifications.clear();
        blockingRelations.clear();

        lostInvestors = false;
        testingPhaseActive = false;
        testingPhaseStartDate = null;
        nextTicketId = 0;
    }
}
