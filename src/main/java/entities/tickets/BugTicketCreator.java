package entities.tickets;

import com.fasterxml.jackson.databind.JsonNode;
import entities.tickets.enums.ExpertiseArea;
import entities.tickets.enums.Frequency;
import entities.tickets.enums.Priority;
import entities.tickets.enums.Severity;

public class BugTicketCreator implements TicketCreator {
    /**
     * Create a BugTicket object using the provided id and JsonNode data
     *
     * @param id the unique identifier for the ticket
     * @param params the JSON parameters for ticket creation
     * @return the created BugTicket object
     */
    @Override
    public Ticket create(final int id, final JsonNode params) {
        return new BugTicket(
                id,
                params.get("title").asText(),
                Priority.valueOf(params.get("businessPriority").asText()),
                ExpertiseArea.valueOf(params.get("expertiseArea").asText()),
                params.has("description") && !params.get("description").isNull()
                        ? params.get("description").asText() : "",
                params.get("reportedBy").asText(),
                params.get("expectedBehavior").asText(),
                params.get("actualBehavior").asText(),
                Frequency.valueOf(params.get("frequency").asText()),
                Severity.valueOf(params.get("severity").asText()),
                params.has("environment") ? params.get("environment").asText() : null,
                params.has("errorCode") ? params.get("errorCode").asInt() : null
        );
    }
}
