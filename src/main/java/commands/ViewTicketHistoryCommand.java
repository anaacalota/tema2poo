package commands;

import app.services.TicketService;
import app.services.UserService;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ViewTicketHistoryCommand extends BaseCommand {

    private final TicketService ticketService;

    public ViewTicketHistoryCommand(final UserService userService,
                                    final TicketService ticketService,
                                    final String username,
                                    final String timestamp) {
        super(userService, username, timestamp);
        this.ticketService = ticketService;
    }

    /**
     * View the history of a ticket
     *
     * @return an ObjectNode containing the ticket history
     */
    @Override
    public ObjectNode execute() {
        return ticketService.viewTicketHistory(username, timestamp);
    }
}
