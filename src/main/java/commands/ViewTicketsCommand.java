package commands;

import app.services.TicketService;
import app.services.UserService;
import app.services.view.ViewTicketService;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.tickets.Ticket;

import java.util.List;

public class ViewTicketsCommand extends BaseCommand {
    private final TicketService ticketService;
    private final ViewTicketService viewTicketService;

    public ViewTicketsCommand(UserService userService,
                              TicketService ticketService,
                              ViewTicketService viewTicketService,
                              String username,
                              String timestamp) {
        super(userService, username, timestamp);
        this.ticketService = ticketService;
        this.viewTicketService = viewTicketService;
    }

    @Override
    public ObjectNode execute() {
        if (!userExists()) {
            return error("viewTickets",
                    "The user " + username + " does not exist.");
        }

        String role = userService.getRole(username);
        List<Ticket> tickets = viewTicketService.getTicketsForUser(role, username);

        ObjectNode result = success("viewTickets");
        ArrayNode array = mapper.createArrayNode();
        for (Ticket ticket : tickets) {
            array.add(ticketService.ticketToJson(ticket));
        }
        result.set("tickets", array);

        return result;
    }
}
