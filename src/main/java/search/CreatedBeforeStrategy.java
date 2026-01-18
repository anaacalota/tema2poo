package search;

import entities.tickets.Ticket;

import java.time.LocalDate;

public class CreatedBeforeStrategy implements SearchStrategy<Ticket> {
    private final LocalDate date;

    public CreatedBeforeStrategy(final LocalDate date) {
        this.date = date;
    }

    /**
     * Checks if the ticket was created before the specified threshold date
     *
     * @param ticket the ticket to evaluate
     * @return true if the ticket's creation date is before the threshold date
     */
    @Override
    public boolean matches(final Ticket ticket) {
        return ticket.getCreatedAt().isBefore(date);
    }
}
