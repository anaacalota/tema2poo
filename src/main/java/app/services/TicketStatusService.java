package app.services;

import app.AppContext;
import entities.tickets.HistoryEntry;
import entities.tickets.Ticket;
import entities.tickets.enums.Action;
import entities.tickets.enums.Status;

import java.time.LocalDate;

public class TicketStatusService {
    private final AppContext context = AppContext.getInstance();

    public TicketStatusService() {

    }

    /**
     * Changes the status of a ticket to the next logical status in the workflow
     *
     * @param ticketId the ID of the ticket to change status for
     * @param username the developer requesting the status change
     * @param date the date of the status change
     * @return null if successful, or an error message if not
     */
    public String changeStatus(final int ticketId, final String username,
                               final LocalDate date) {
        if (!context.ticketExists(ticketId)) {
            return "Ticket " + ticketId + " does not exist.";
        }

        Ticket ticket = context.getTicket(ticketId);

        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().equals(username)) {
            return "Ticket " + ticketId + " is not assigned to developer " + username + ".";
        }

        if (ticket.getStatus() == Status.CLOSED) {
            return null;
        }

        Status oldStatus = ticket.getStatus();
        ticket.pushPreviousStatus(oldStatus);

        Status next = switch (oldStatus) {
            case OPEN -> Status.IN_PROGRESS;
            case IN_PROGRESS -> Status.RESOLVED;
            case RESOLVED -> Status.CLOSED;
            default -> oldStatus;
        };

        ticket.setStatus(next);

        if (next == Status.RESOLVED) {
            ticket.setSolvedAt(date);
        }

        HistoryEntry historyEntry = new HistoryEntry(
                Action.STATUS_CHANGED,
                username,
                date
        );
        historyEntry.setFromStatus(oldStatus);
        historyEntry.setToStatus(next);
        ticket.addHistoryEntry(historyEntry);

        return null;
    }

    /**
     * Undoes the last status change of a ticket, reverting it to its previous status
     *
     * @param ticketId the ID of the ticket to revert status for
     * @param username the developer requesting the undo
     * @param date the date of the undo action
     * @return null if successful, or an error message if not
     */
    public String undoChangeStatus(final int ticketId, final String username,
                                   final LocalDate date) {
        if (!context.ticketExists(ticketId)) {
            return "Ticket " + ticketId + " does not exist.";
        }

        Ticket ticket = context.getTicket(ticketId);

        if (!username.equals(ticket.getAssignedTo())) {
            return "Ticket " + ticketId + " is not assigned to developer " + username + ".";
        }

        if (!ticket.hasPreviousStatus()) {
            return "No previous status to revert to for ticket " + ticketId + ".";
        }

        Status current = ticket.getStatus();
        Status previous = ticket.popPreviousStatus();
        ticket.setStatus(previous);

        if (previous != Status.RESOLVED) {
            ticket.setSolvedAt(null);
        }

        HistoryEntry historyEntry = new HistoryEntry(Action.STATUS_CHANGED, username, date);
        historyEntry.setFromStatus(current);
        historyEntry.setToStatus(previous);
        ticket.addHistoryEntry(historyEntry);

        return null;
    }
}
