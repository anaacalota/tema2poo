package commands;

import app.AppContext;
import app.services.FlowService;
import app.services.UserService;
import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.milestones.Milestone;
import entities.tickets.Ticket;
import entities.tickets.enums.Status;

public class StartTestingPhaseCommand implements Command {
    private final String timestamp;
    private final String username;
    private final UserService userService;

    public StartTestingPhaseCommand(final UserService userService, final String username,
                                    final String timestamp) {
        this.userService = userService;
        this.username = username;
        this.timestamp = timestamp;
    }

    /**
     * Start a new testing phase
     *
     * @return null
     */
    @Override
    public ObjectNode execute() {
        AppContext context = AppContext.getInstance();
        ObjectNode result = ResultUtils.createBaseNode("startTestingPhase", username, timestamp);
        if (!userService.userExists(username)) {
            result.put("error", "The user " + username + " does not exist.");
            return result;
        }
        if (!userService.hasRole(username, "MANAGER")) {
            result.put("error", "The user does not have permission to execute this command: "
                    + "required role MANAGER; user role " + userService.getRole(username) + ".");
            return result;
        }
        for (Milestone milestone : context.getAllMilestones()) {
            for (int ticketId : milestone.getTickets()) {
                Ticket ticket = context.getTicket(ticketId);
                if (ticket != null && ticket.getStatus() != Status.CLOSED) {
                    result.put("error", "Cannot start a new testing phase.");
                    return result;
                }
            }
        }
        FlowService flowService = new FlowService();
        flowService.startNewTestingPhase(timestamp);
        context.setTestingPhaseActive(true);

        return null;
    }
}