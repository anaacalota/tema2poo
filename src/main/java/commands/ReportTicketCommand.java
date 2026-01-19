package commands;

import app.services.TicketService;
import app.services.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ReportTicketCommand extends BaseCommand {
    private final TicketService ticketService;
    private final JsonNode params;

    public ReportTicketCommand(final UserService userService,
                               final TicketService ticketService,
                               final String username,
                               final String timestamp,
                               final JsonNode params) {
        super(userService, username, timestamp);
        this.ticketService = ticketService;
        this.params = params;
    }

    /**
     * Report a ticket
     *
     * @return an ObjectNode containing the ticket id
     */
    @Override
    public ObjectNode execute() {
        if (!userExists()) {
            return error("reportTicket",
                    "The user " + username + " does not exist.");
        }

        if (!hasRole("REPORTER")) {
            return error("reportTicket",
                    "The user does not have permission to execute this command: "
                            + "required role REPORTER; user role "
                            + userService.getRole(username) + ".");
        }

        return ticketService.reportTicket(username, timestamp, params);
    }
}
