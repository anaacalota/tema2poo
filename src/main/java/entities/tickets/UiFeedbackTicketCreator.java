package entities.tickets;

import com.fasterxml.jackson.databind.JsonNode;
import entities.tickets.enums.BusinessValue;
import entities.tickets.enums.ExpertiseArea;
import entities.tickets.enums.Priority;

public class UiFeedbackTicketCreator implements TicketCreator {
    /**
     * Create a UiFeedbackTicket object using the provided id and JsonNode data
     *
     * @param id the unique identifier for the ticket
     * @param params the JSON parameters for ticket creation
     * @return the created UiFeedbackTicket object
     */
    @Override
    public Ticket create(final int id, final JsonNode params) {
        return new UiFeedbackTicket(
                id,
                params.get("title").asText(),
                Priority.valueOf(params.get("businessPriority").asText()),
                ExpertiseArea.valueOf(params.get("expertiseArea").asText()),
                params.has("description") && !params.get("description").isNull()
                        ? params.get("description").asText() : "",
                params.get("reportedBy").asText(),
                params.has("uiElementId") ? params.get("uiElementId").asText() : null,
                BusinessValue.valueOf(params.get("businessValue").asText()),
                params.get("usabilityScore").asInt(),
                params.has("screenshotUrl") ? params.get("screenshotUrl").asText() : null,
                params.has("suggestedFix") ? params.get("suggestedFix").asText() : null
        );
    }
}
