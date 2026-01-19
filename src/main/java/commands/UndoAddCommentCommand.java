package commands;

import app.services.TicketService;
import app.services.UserService;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class UndoAddCommentCommand extends BaseCommand {
    private final TicketService ticketService;
    private final int ticketId;

    public UndoAddCommentCommand(final UserService userService,
                                 final TicketService ticketService,
                                 final String username,
                                 final String timestamp,
                                 final int ticketId) {
        super(userService, username, timestamp);
        this.ticketService = ticketService;
        this.ticketId = ticketId;
    }

    /**
     * Undo the last comment added to a ticket
     *
     * @return an ObjectNode containing the ticket id
     */
    @Override
    public ObjectNode execute() {
        return ticketService.undoAddComment(username, timestamp, ticketId);
    }
}
