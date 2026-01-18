package commands;

import app.services.TicketService;
import app.services.UserService;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ViewTicketHistoryCommand extends BaseCommand {

    private final TicketService ticketService;

    public ViewTicketHistoryCommand(UserService userService,
                                    TicketService ticketService,
                                    String username,
                                    String timestamp) {
        super(userService, username, timestamp);
        this.ticketService = ticketService;
    }

    @Override
    public ObjectNode execute() {
        return ticketService.viewTicketHistory(username, timestamp);
    }
}
