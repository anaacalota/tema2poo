package commands;

import app.services.TicketService;
import app.services.UserService;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ViewAssignedTicketsCommand extends BaseCommand {

    private final TicketService ticketService;

    public ViewAssignedTicketsCommand(UserService userService,
                                      TicketService ticketService,
                                      String username,
                                      String timestamp) {
        super(userService, username, timestamp);
        this.ticketService = ticketService;
    }

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
