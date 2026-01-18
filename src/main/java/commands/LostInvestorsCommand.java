package commands;

import app.services.FlowService;
import app.services.UserService;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class LostInvestorsCommand extends BaseCommand {
    private final FlowService flowService;

    public LostInvestorsCommand(UserService userService,
                                FlowService flowService,
                                String username,
                                String timestamp) {
        super(userService, username, timestamp);
        this.flowService = flowService;
    }

    @Override
    public ObjectNode execute() {
        if (!userExists()) {
            return error("lostInvestors",
                    "The user " + username + " does not exist.");
        }

        if (!hasRole("MANAGER")) {
            return error("lostInvestors",
                    "The user does not have permission to execute this command: " +
                            "required role MANAGER; user role " + userService.getRole(username) + ".");
        }

        flowService.loseInvestors();
        return null;
    }
}
