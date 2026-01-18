package commands;

import app.services.TicketService;
import app.services.UserService;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class UndoAddCommentCommand extends BaseCommand {

    private final TicketService ticketService;
    private final int ticketId;

    public UndoAddCommentCommand(UserService userService,
                                 TicketService ticketService,
                                 String username,
                                 String timestamp,
                                 int ticketId) {
        super(userService, username, timestamp);
        this.ticketService = ticketService;
        this.ticketId = ticketId;
    }

    @Override
    public ObjectNode execute() {
        return ticketService.undoAddComment(username, timestamp, ticketId);
    }
}
