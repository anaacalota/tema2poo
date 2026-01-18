package commands;

import app.services.TicketStatusService;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDate;

public class ChangeStatusCommand extends BaseCommand {
    private final int ticketId;

    public ChangeStatusCommand(final String username,
                               final String timestamp,
                               final int ticketId) {
        super(null, username, timestamp);
        this.ticketId = ticketId;
    }

    /**
     * Change the status of a ticket
     *
     * @return an error ObjectNode if validation fails, or null on successful change
     */
    @Override
    public ObjectNode execute() {
        TicketStatusService service = new TicketStatusService();
        String errorMessage = service.changeStatus(ticketId, username,
                LocalDate.parse(timestamp));

        if (errorMessage != null) {
            return error("changeStatus", errorMessage);
        }

        return null;
    }
}
