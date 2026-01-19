package commands;

import app.AppContext;
import app.services.TicketStatusService;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDate;

public class UndoChangeStatusCommand extends BaseCommand {
    private final int ticketId;

    public UndoChangeStatusCommand(final String username, final String timestamp,
                                   final int ticketId) {
        super(null, username, timestamp);
        this.ticketId = ticketId;
    }

    /**
     * Undo the change of status of a ticket
     *
     * @return an ObjectNode containing the ticket id
     */
    @Override
    public ObjectNode execute() {
        TicketStatusService service = new TicketStatusService();
        String errorMessage = service.undoChangeStatus(ticketId, username,
                LocalDate.parse(timestamp));

        if (errorMessage != null) {
            return error("undoChangeStatus", errorMessage);
        }

        return null;
    }
}
