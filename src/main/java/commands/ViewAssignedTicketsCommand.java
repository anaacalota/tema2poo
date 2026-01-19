package commands;

import app.services.TicketService;
import app.services.UserService;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ViewAssignedTicketsCommand extends BaseCommand {

    private final TicketService ticketService;

    public ViewAssignedTicketsCommand(final UserService userService,
                                      final TicketService ticketService,
                                      final String username,
                                      final String timestamp) {
        super(userService, username, timestamp);
        this.ticketService = ticketService;
    }

    /**
     * View all tickets assigned to a developer
     *
     * @return an ObjectNode containing the command name, username, and timestamp,
     */
    @Override
    public ObjectNode execute() {

        if (!userExists()) {
            return error("viewAssignedTickets",
                    "The user " + username + " does not exist.");
        }

        if (!hasRole("DEVELOPER")) {
            return error("viewAssignedTickets",
                    "The user does not have permission to execute this command: required role DEVELOPER; user role "
                            + userService.getRole(username) + ".");
        }

        return ticketService.viewAssignedTickets(username, timestamp);
    }
}
