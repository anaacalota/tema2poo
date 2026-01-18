package search;

import entities.tickets.Ticket;

public class TypeStrategy implements SearchStrategy<Ticket> {
    private final String type;

    public TypeStrategy(final String type) {
        this.type = type;
    }

    /**
     * Verifies if the ticket's type matches the target type
     *
     * @param ticket the ticket to evaluate
     * @return true if the ticket's type is equal to the target type
     */
    @Override
    public boolean matches(final Ticket ticket) {
        return ticket.getType().equals(type);
    }
}
