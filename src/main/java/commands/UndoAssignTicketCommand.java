package commands;

import app.services.TicketService;
import app.services.UserService;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class UndoAssignTicketCommand extends BaseCommand {

    private final TicketService ticketService;
    private final int ticketId;

    public UndoAssignTicketCommand(UserService userService,
                                   TicketService ticketService,
                                   String username,
                                   String timestamp,
                                   int ticketId) {
        super(userService, username, timestamp);
        this.ticketService = ticketService;
        this.ticketId = ticketId;
    }

    @Override
    public ObjectNode execute() {

        if (!userExists()) {
            return error("undoAssignTicket",
                    "The user " + username + " does not exist.");
        }

        if (!hasRole("DEVELOPER")) {
            return error("undoAssignTicket",
                    "The user does not have permission to execute this command: required role DEVELOPER; user role "
                            + userService.getRole(username) + ".");
        }

        return ticketService.undoAssignTicket(username, timestamp, ticketId);
    }
}
