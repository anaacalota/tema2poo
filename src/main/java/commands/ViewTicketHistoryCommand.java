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
        if (!userExists()) {
            return error("viewTicketHistory", "The user " + username + " does not exist.");
        }
        if (!hasRole("DEVELOPER", "MANAGER")) {
            return error("viewTicketHistory", "The user does not have permission to execute "
                    + "this command: required role DEVELOPER, MANAGER; user role "
                    + userService.getRole(username) + ".");
        }
        return ticketService.viewTicketHistory(username, timestamp);
    }
}
