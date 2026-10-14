package entities.milestones;

import app.AppContext;
import entities.tickets.Ticket;
import entities.tickets.enums.Status;
import lombok.Getter;
import lombok.Setter;
import notifications.Subject;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Milestone extends Subject {
    @Getter
    private final String name;
    @Getter
    private final List<String> blockingFor;
    @Getter
    private final LocalDate dueDate;
    @Getter
    private final List<Integer> tickets;
    @Getter
    private final List<String> assignedDevs;
    @Getter
    private final String createdBy;
    @Getter
    private final LocalDate createdAt;
    private boolean wasBlockedBefore = false;
    private boolean priorityResetAfterUnblock = false;
    @Getter
    private boolean unblockedHandled = false;
    @Getter
    private boolean dueTomorrowHandled = false;
    @Getter @Setter
    private int lastChangeLevel = 0;

    public Milestone(final String name, final String dueDate,
                     final List<Integer> tickets, final List<String> assignedDevs,
                     final List<String> blockingFor, final String createdBy,
                     final String createdAt) {
        this.name = name;
        this.dueDate = LocalDate.parse(dueDate);
        this.tickets = new ArrayList<>(tickets);
        this.assignedDevs = new ArrayList<>(assignedDevs);
        if (blockingFor != null) {
            this.blockingFor = new ArrayList<>(blockingFor);
        } else {
            this.blockingFor = new ArrayList<>();
        }
        this.createdBy = createdBy;
        this.createdAt = LocalDate.parse(createdAt);
    }

    /**
     * Checks if the milestone is still active (has unresolved tickets)
     *
     * @return true if at least one ticket is not closed, false otherwise
     */
    public boolean isActive() {
        for (int id : tickets) {
            Ticket ticket = AppContext.getInstance().getTicket(id);
            if (ticket != null && ticket.getStatus() != Status.CLOSED) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates how many days the milestone is overdue relative to a given date
     *
     * @param currentDate the date to check against the due date
     * @return the number of overdue days, or 0 if not overdue
     */
    public int getOverdueBy(final LocalDate currentDate) {
        if (currentDate.isAfter(dueDate)) {
            return (int) ChronoUnit.DAYS.between(dueDate, currentDate) + 1;
        }
        return 0;
    }

    /**
     * Notifies observers that the milestone has been created
     */
    public void notifyCreated() {
        notifyObservers("New milestone " + name
                + " has been created with due date " + dueDate + ".");
    }

    /**
     * Notifies observers that the milestone is due tomorrow
     */
    public void notifyDueTomorrow() {
        notifyObservers("Milestone " + name
                + " is due tomorrow. All unresolved tickets are now CRITICAL.");
    }

    /**
     * Notifies observers that the milestone was unblocked after its due date
     */
    public void notifyUnblockedAfterDueDate() {
        notifyObservers("Milestone " + name
                + " was unblocked after due date. All active tickets are now CRITICAL.");
    }

    /** Notifies assigned developers when this milestone is unblocked. */
    public void notifyUnblockedByTicket(final int ticketId) {
        notifyObservers("Milestone " + name + " is now unblocked as ticket " + ticketId
                + " has been CLOSED.");
    }

    /**
     * Indicates whether the milestone was previously in a blocked state
     *
     * @return true if the milestone was blocked before, false otherwise
     */
    public boolean wasPreviouslyBlocked() {
        return wasBlockedBefore;
    }

    /**
     * Records that the milestone is currently in a blocked state
     */
    public void markBlocked() {
        wasBlockedBefore = true;
    }

    public boolean wasPriorityResetAfterUnblock() {
        return priorityResetAfterUnblock;
    }

    public void markPriorityResetAfterUnblock() {
        priorityResetAfterUnblock = true;
    }

    /**
     * Sets the flag indicating that the unblocking notification has been processed
     */

    public void markUnblockedHandled() {
        unblockedHandled = true;
    }

    /**
     * Marks that the due tomorrow notifications and actions have been handled
     * for this milestone
     */
    public void markDueTomorrowHandled() {
        dueTomorrowHandled = true;
    }

}
