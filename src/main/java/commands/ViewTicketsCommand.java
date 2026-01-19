package commands;

import app.services.UserService;
import app.services.view.ViewTicketService;
import entities.tickets.Ticket;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class ViewTicketsCommand extends BaseCommand {
    private final ViewTicketService viewTicketService;

    public ViewTicketsCommand(final UserService userService,
                              final ViewTicketService viewTicketService,
                              final String username,
                              final String timestamp) {
        super(userService, username, timestamp);
        this.viewTicketService = viewTicketService;
    }

    /**
     * List all tickets assigned to a user
     *
     * @return an ObjectNode containing the command name, username, and timestamp,
     * as well as the list of tickets
     */
    @Override
    public ObjectNode execute() {
        if (!userExists()) {
            return error("viewTickets",
                    "The user " + username + " does not exist.");
        }

        List<Ticket> tickets = viewTicketService.getTicketsForUser(userService, username);

        ObjectNode result = success("viewTickets");
        result = viewTicketService.printTickets(result, tickets);

        return result;
    }
}
