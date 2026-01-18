package commands;

import app.services.TicketService;
import app.services.UserService;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AssignTicketCommand extends BaseCommand {
    private final TicketService ticketService;
    private final int ticketId;

    public AssignTicketCommand(final UserService userService,
                               final TicketService ticketService,
                               final String username,
                               final String timestamp,
                               final int ticketId) {
        super(userService, username, timestamp);
        this.ticketService = ticketService;
        this.ticketId = ticketId;
    }

    /**
     * Assign a ticket to a developer
     *
     * @return an error ObjectNode if validation fails, or null on successful assignment
     */
    @Override
    public ObjectNode execute() {
        if (!userExists()) {
            return error("assignTicket", "The user " + username + " does not exist.");
        }

        if (!hasRole("DEVELOPER")) {
            return error("assignTicket", "The user does not have permission" +
                    "to execute this command: required role DEVELOPER; user role "
                    + userService.getRole(username) + ".");
        }

        return ticketService.assignTicket(username, timestamp, ticketId);
    }
}
