package search;

import entities.tickets.Ticket;
import entities.tickets.enums.Priority;

public class BusinessPriorityStrategy implements SearchStrategy<Ticket> {
    private final Priority priority;

    public BusinessPriorityStrategy(final Priority priority) {
        this.priority = priority;
    }

    /**
     * Verifies if the ticket's priority matches the required priority
     *
     * @param ticket the ticket to evaluate
     * @return true if the ticket's priority is equal to the target priority
     */
    @Override
    public boolean matches(final Ticket ticket) {
        return ticket.getBusinessPriority() == priority;
    }
}
