package entities.tickets;

import fileio.TicketInput;

public class BugTicketCreator implements TicketCreator {
    /**
     * Create a BugTicket object using the provided id and TicketInput data
     *
     * @param id the unique identifier for the ticket
     * @param ticketInput the input data for the ticket
     * @return the created BugTicket object
     */
    @Override
    public Ticket create(final int id, final TicketInput ticketInput) {
        return new BugTicket(
                id,
                ticketInput.getTitle(),
                ticketInput.getBusinessPriority(),
                ticketInput.getExpertiseArea(),
                ticketInput.getDescription(),
                ticketInput.getReportedBy(),
                ticketInput.getExpectedBehavior(),
                ticketInput.getActualBehavior(),
                ticketInput.getFrequency(),
                ticketInput.getSeverity(),
                ticketInput.getEnvironment(),
                ticketInput.getErrorCode()
        );
    }
}
