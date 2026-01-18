package entities.tickets;

import fileio.TicketInput;

public class UiFeedbackTicketCreator implements TicketCreator {
    /**
     * Create a UiFeedbackTicket object using the provided id and TicketInput data
     *
     * @param id the unique identifier for the ticket
     * @param ticketInput the input data for the ticket
     * @return the created UiFeedbackTicket object
     */
    @Override
    public Ticket create(final int id, final TicketInput ticketInput) {
        return new UiFeedbackTicket(
                id,
                ticketInput.getTitle(),
                ticketInput.getBusinessPriority(),
                ticketInput.getExpertiseArea(),
                ticketInput.getDescription(),
                ticketInput.getReportedBy(),
                ticketInput.getUiElementId(),
                ticketInput.getBusinessValue(),
                ticketInput.getUsabilityScore(),
                ticketInput.getScreenshotUrl(),
                ticketInput.getSuggestedFix()
        );
    }
}
