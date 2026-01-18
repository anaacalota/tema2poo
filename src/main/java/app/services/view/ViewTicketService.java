package app.services.view;

import app.services.TicketService;
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
     * @param role the role of the user
     * @param username the username of the user
     * @return the list of tickets visible to the user
     */
    public List<Ticket> getTicketsForUser(final String role, final String username) {
        List<Ticket> tickets;

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
}
