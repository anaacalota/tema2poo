package entities.tickets;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Factory class for creating Ticket objects based on their type
 */
public final class TicketFactory {
    private TicketFactory() {

    }
    /**
     * Creates a Ticket object based on the provided params
     *
     * @param id the unique identifier for the ticket
     * @param params the JSON parameters for ticket creation
     * @return the created Ticket object
     */
    public static Ticket createTicket(final int id, final JsonNode params) {
        String type = params.get("type").asText();

        TicketCreator creator = switch (type) {
            case "BUG" -> new BugTicketCreator();
            case "FEATURE_REQUEST" -> new FeatureRequestTicketCreator();
            case "UI_FEEDBACK" -> new UiFeedbackTicketCreator();
            default -> throw new IllegalArgumentException("Unknown ticket type: " + type);
        };

        return creator.create(id, params);
    }
}
