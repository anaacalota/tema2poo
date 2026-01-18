package entities.tickets;

import fileio.TicketInput;

/**
 * Factory class for creating Ticket objects based on their type
 */
public final class TicketFactory {
    private TicketFactory() {

    }
    /**
     * Creates a Ticket object based on the provided TicketInput
     *
     * @param id the unique identifier for the ticket
     * @param ticketInput the input data for the ticket
     * @return the created Ticket object
     */
    public static Ticket getTicket(final int id, final TicketInput ticketInput) {
        String type = ticketInput.getType();

        TicketCreator creator = switch (type) {
            case "BUG" -> new BugTicketCreator();
            case "FEATURE_REQUEST" -> new FeatureRequestTicketCreator();
            case "UI_FEEDBACK" -> new UiFeedbackTicketCreator();
            default -> throw new IllegalArgumentException("Unknown ticket type: " + type);
        };

        return creator.create(id, ticketInput);
    }
}
