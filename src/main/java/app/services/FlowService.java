package app.services;

import app.AppContext;
import entities.tickets.HistoryEntry;
import entities.tickets.enums.Action;
import entities.milestones.Milestone;
import entities.tickets.Ticket;
import entities.tickets.enums.Priority;
import entities.tickets.enums.Status;
import entities.users.Developer;
import entities.users.User;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FlowService {
    private final AppContext context;
    public static final int TESTING_PHASE_DURATION_DAYS = 12;

    public FlowService() {
        this.context = AppContext.getInstance();
    }

    /**
     * Initializes a new testing phase by setting its start date
     *
     * @param timestamp the date string to be set as the start of the testing phase
     */
    public void startNewTestingPhase(final String timestamp) {
        context.setTestingPhaseStartDate(LocalDate.parse(timestamp));
    }

    /**
     * Determines if the system is currently in a testing phase based on the provided time
     * A phase lasts for 12 days from the start date
     *
     * @param timestamp the current command timestamp to check
     * @return true if within the testing period and investors are not lost
     */
    public boolean isTestingPhase(final String timestamp) {
        if (context.isLostInvestors()) {
            return false;
        }

        LocalDate current = LocalDate.parse(timestamp);
        if (context.getTestingPhaseStartDate() == null) {
            context.setTestingPhaseStartDate(current);
            return true;
        }

        long daysPassed = ChronoUnit.DAYS.between(context.getTestingPhaseStartDate(), current) + 1;
        return daysPassed <= TESTING_PHASE_DURATION_DAYS;
    }

    /**
     * Flags the system state to indicate that investors are lost
     */
    public void loseInvestors() {
        context.setLostInvestors(true);
    }

    /**
     * Checks if the investors are lost
     *
     * @return true if investors are lost, false otherwise
     */
    public boolean investorsLost() {
        return context.isLostInvestors();
    }

    /**
     * Identifies milestones due the next day, sends notifications,
     * and sets all their active tickets to CRITICAL
     *
     * @param timestamp the current timestamp
     */
    public void checkMilestonesDueTomorrow(final String timestamp) {
        LocalDate today = LocalDate.parse(timestamp);

        for (Milestone milestone : context.getAllMilestones()) {
            LocalDate due = milestone.getDueDate();

            if (due != null && !context.isMilestoneBlocked(milestone.getName())
                    && due.minusDays(1).equals(today)
                    && !milestone.isDueTomorrowHandled()) {

                // send notifications
                milestone.notifyDueTomorrow();
                milestone.markDueTomorrowHandled();

                // all active tickets become CRITICAL
                for (int id : milestone.getTickets()) {
                    Ticket ticket = context.getTicket(id);
                    if (ticket != null && (ticket.getStatus() == Status.OPEN
                            || ticket.getStatus() == Status.IN_PROGRESS)) {
                        ticket.setBusinessPriority(Priority.CRITICAL);
                        reopenIfDeveloperCannotHandle(ticket, today);
                    }
                }
            }
        }
    }

    /**
     * Processes milestones that were previously blocked but are now unblocked after
     * their due date, making their tickets CRITICAL and notifying developers
     *
     * @param timestamp the current system timestamp
     */
    public void checkMilestonesUnblockedAfterDueDate(final String timestamp) {
        LocalDate today = LocalDate.parse(timestamp);

        for (Milestone milestone : context.getAllMilestones()) {
            boolean isBlockedNow = context.isMilestoneBlocked(milestone.getName());

            // mark all blocked milestones
            if (isBlockedNow) {
                milestone.markBlocked();
                continue;
            }

            // if it was blocked before, but not anymore
            if (milestone.wasPreviouslyBlocked()
                    && !milestone.isUnblockedHandled()
                    && today.isAfter(milestone.getDueDate())) {

                // send notifications
                milestone.notifyUnblockedAfterDueDate();
                milestone.markUnblockedHandled();

                // mark all active tickets as CRITICAL
                for (int id : milestone.getTickets()) {
                    Ticket ticket = context.getTicket(id);
                    if (ticket != null && (ticket.getStatus() == Status.OPEN
                            || ticket.getStatus() == Status.IN_PROGRESS)) {
                        ticket.setBusinessPriority(Priority.CRITICAL);
                        reopenIfDeveloperCannotHandle(ticket, today);
                    }
                }
            }
        }
    }

    /** Reopens an assigned ticket if a priority increase exceeds its developer's access. */
    public void reopenIfDeveloperCannotHandle(final Ticket ticket, final LocalDate date) {
        if (ticket.getAssignedTo() == null || ticket.getStatus() == Status.CLOSED) {
            return;
        }
        User user = context.getUser(ticket.getAssignedTo());
        if (user != null
                && !user.canHandleTicket(ticket.getType(), ticket.getBusinessPriority())) {
            String developerName = ticket.getAssignedTo();
            ticket.setStatus(Status.OPEN);
            ticket.setAssignedTo(null);
            ticket.setAssignedAt(null);
            ticket.removeObserver(user);
            HistoryEntry entry = new HistoryEntry(Action.REMOVED_FROM_DEV, "system", date);
            entry.setDeveloperName(developerName);
            ticket.addHistoryEntry(entry);
        }
    }

}
