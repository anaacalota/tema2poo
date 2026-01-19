package entities.tickets;

import com.fasterxml.jackson.databind.JsonNode;

public interface TicketCreator {
    /**
     * Creates a Ticket object based on the provided JsonNode
     *
     * @param id the unique identifier for the ticket
     * @param params the JSON parameters for ticket creation
     * @return the created Ticket object
     */
    Ticket create(int id, JsonNode params);
}
