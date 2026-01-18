package entities.tickets;

import fileio.TicketInput;

public class FeatureRequestTicketCreator implements TicketCreator {
    /**
     * Create a FeatureRequestTicket object using the provided id and TicketInput data
     *
     * @param id the unique identifier for the ticket
     * @param ticketInput the input data for the ticket
     * @return the created FeatureRequestTicket object
     */
    @Override
    public Ticket create(final int id, final TicketInput ticketInput) {
        return new FeatureRequestTicket(
                id,
                ticketInput.getTitle(),
                ticketInput.getBusinessPriority(),
                ticketInput.getExpertiseArea(),
                ticketInput.getDescription(),
                ticketInput.getReportedBy(),
                ticketInput.getBusinessValue(),
                ticketInput.getCustomerDemand()
        );
    }
}
