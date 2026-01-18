package commands;

import app.services.TicketService;
import app.services.UserService;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AddCommentCommand extends BaseCommand {
    private final TicketService ticketService;
    private final int ticketId;
    private final String content;

    public AddCommentCommand(final UserService userService,
                             final TicketService ticketService,
                             final String username,
                             final String timestamp,
                             final int ticketId,
                             final String content) {
        super(userService, username, timestamp);
        this.ticketService = ticketService;
        this.ticketId = ticketId;
        this.content = content;
    }

    /**
     * Add a comment to a ticket
     * @return an error ObjectNode if validation fails, or null on successful creation
     */
    @Override
    public ObjectNode execute() {
        return ticketService.addComment(username, timestamp, ticketId, content);
    }
}
