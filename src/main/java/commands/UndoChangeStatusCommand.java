package commands;

import app.services.TicketStatusService;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDate;

public class UndoChangeStatusCommand extends BaseCommand {

    private final int ticketId;

    public UndoChangeStatusCommand(String username, String timestamp, int ticketId) {
        super(null, username, timestamp);
        this.ticketId = ticketId;
    }

    @Override
    public ObjectNode execute() {
        TicketStatusService service = new TicketStatusService();
        String errorMessage = service.undoChangeStatus(ticketId, username, LocalDate.parse(timestamp));

        if (errorMessage != null) {
            return error("undoChangeStatus", errorMessage);
        }

        return null;
    }
}
