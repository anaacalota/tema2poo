package search;

import entities.tickets.Ticket;

import java.time.LocalDate;

public class CreatedAfterStrategy implements SearchStrategy<Ticket> {
    private final LocalDate date;

    public CreatedAfterStrategy(final LocalDate date) {
        this.date = date;
    }

    /**
     * Checks if the ticket was created after the specified threshold date
     *
     * @param ticket the ticket to evaluate
     * @return true if the ticket's creation date is after the threshold date
     */
    @Override
    public boolean matches(final Ticket ticket) {
        return ticket.getCreatedAt().isAfter(date);
    }
}
