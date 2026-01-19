package commands;

import app.services.TicketService;
import app.services.UserService;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class UndoAssignTicketCommand extends BaseCommand {

    private final TicketService ticketService;
    private final int ticketId;

    public UndoAssignTicketCommand(final UserService userService,
                                   final TicketService ticketService,
                                   final String username,
                                   final String timestamp,
                                   final int ticketId) {
        super(userService, username, timestamp);
        this.ticketService = ticketService;
        this.ticketId = ticketId;
    }

    /**
     * Undo the assignment of a ticket to a developer
     *
     * @return an ObjectNode containing the ticket id
     */
    @Override
    public ObjectNode execute() {
        if (!userExists()) {
            return error("undoAssignTicket",
                    "The user " + username + " does not exist.");
        }

        if (!hasRole("DEVELOPER")) {
            return error("undoAssignTicket",
                    "The user does not have permission to execute"
                            + "this command: required role DEVELOPER; user role "
                            + userService.getRole(username) + ".");
        }

        return ticketService.undoAssignTicket(username, timestamp, ticketId);
    }
}
