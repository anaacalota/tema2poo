package entities.tickets;

import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.tickets.enums.Action;
import entities.tickets.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

public class HistoryEntry {
    @Getter
    private final Action action;
    @Getter
    private final String by;
    @Getter
    private final LocalDate timestamp;
    @Setter
    private Status fromStatus;
    @Setter
    private Status toStatus;
    @Setter
    private String milestoneName;

    public HistoryEntry(final Action action, final String by, final LocalDate timestamp) {
        this.action = action;
        this.by = by;
        this.timestamp = timestamp;
    }

    /**
     * Converts the history entry to a JSON object
     *
     * @return the JSON representation of the history entry
     */
    public ObjectNode toJson() {
        ObjectMapper mapper = ResultUtils.MAPPER;
        ObjectNode node = mapper.createObjectNode();

        if (action.equals(Action.DE_ASSIGNED)) {
            node.put("action", "DE-ASSIGNED");
        } else {
            node.put("action", action.name());
        }
        node.put("timestamp", timestamp.toString());
        node.put("by", by);

        switch (action) {
            case STATUS_CHANGED:
                node.put("from", fromStatus.name());
                node.put("to", toStatus.name());
                break;

            case ADDED_TO_MILESTONE:
                node.put("milestone", milestoneName);
                break;
            default:
                break;
        }

        return node;
    }
}
