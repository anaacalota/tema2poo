package app.services.view;

import app.services.TicketService;
import app.services.UserService;
import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.tickets.Ticket;

import java.util.Comparator;
import java.util.List;

public class ViewTicketService {
    private final TicketService ticketService;

    public ViewTicketService(final TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Returns the list of tickets visible to a user based on their role
     *
     * @param username the username of the user
     * @return the list of tickets visible to the user
     */
    public List<Ticket> getTicketsForUser(final UserService userService, final String username) {
        List<Ticket> tickets;
        String role = userService.getRole(username);

        switch (role) {
            case "DEVELOPER" -> tickets = ticketService.ticketsForDeveloper(username);
            case "MANAGER" -> tickets = ticketService.ticketsForManager();
            case "REPORTER" -> tickets = ticketService.ticketsForReporter(username);
            default -> tickets = List.of();
        }

        tickets.sort(Comparator.comparing(Ticket::getCreatedAt)
                                          .thenComparingInt(Ticket::getId));

        return tickets;
    }

    /**
     * Method to print tickets in JSON format
     *
     * @param result the JSON object to be populated
     * @param tickets the list of tickets to be printed
     * @return the populated JSON object
     */
    public ObjectNode printTickets(final ObjectNode result, final List<Ticket> tickets) {
        ArrayNode array = ResultUtils.MAPPER.createArrayNode();
        for (Ticket ticket : tickets) {
            array.add(ticketService.ticketToJson(ticket));
        }
        result.set("tickets", array);
        return result;
    }
}
