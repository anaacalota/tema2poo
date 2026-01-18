package search;

import entities.tickets.Ticket;
import entities.tickets.enums.Status;
import entities.users.Developer;

public class AvailableForAssignmentStrategy implements SearchStrategy<Ticket> {
    private final Developer dev;

    public AvailableForAssignmentStrategy(final Developer dev) {
        this.dev = dev;
    }

    /**
     * Validates if a ticket can be assigned to the developer
     *
     * @param ticket the ticket to evaluate
     * @return true if the ticket is OPEN and matches the developer's profile
     */
    @Override
    public boolean matches(final Ticket ticket) {
        return ticket.getStatus() == Status.OPEN
                && dev.canHandleExpertiseArea(ticket.getExpertiseArea())
                && dev.canHandleTicket(ticket.getType(), ticket.getBusinessPriority());
    }
}
