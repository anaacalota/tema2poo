package entities.tickets;

import com.fasterxml.jackson.databind.JsonNode;
import entities.tickets.enums.BusinessValue;
import entities.tickets.enums.CustomerDemand;
import entities.tickets.enums.ExpertiseArea;
import entities.tickets.enums.Priority;

public class FeatureRequestTicketCreator implements TicketCreator {
    /**
     * Create a FeatureRequestTicket object using the provided id and JsonNode data
     *
     * @param id the unique identifier for the ticket
     * @param params the JSON parameters for ticket creation
     * @return the created FeatureRequestTicket object
     */
    @Override
    public Ticket create(final int id, final JsonNode params) {
        return new FeatureRequestTicket(
                id,
                params.get("title").asText(),
                Priority.valueOf(params.get("businessPriority").asText()),
                ExpertiseArea.valueOf(params.get("expertiseArea").asText()),
                params.has("description") && !params.get("description").isNull()
                        ? params.get("description").asText() : "",
                params.get("reportedBy").asText(),
                BusinessValue.valueOf(params.get("businessValue").asText()),
                CustomerDemand.valueOf(params.get("customerDemand").asText())
        );
    }
}
