package commands;

import app.services.FlowService;
import app.services.MilestoneService;
import app.services.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CreateMilestoneCommand extends BaseCommand {
    private final MilestoneService milestoneService;
    private final FlowService flowService;
    private final String name;
    private final String dueDate;
    private final JsonNode params;

    public CreateMilestoneCommand(UserService userService,
                                  MilestoneService milestoneService,
                                  FlowService flowService,
                                  String username,
                                  String timestamp,
                                  String name,
                                  String dueDate,
                                  JsonNode params) {
        super(userService, username, timestamp);
        this.milestoneService = milestoneService;
        this.flowService = flowService;
        this.name = name;
        this.dueDate = dueDate;
        this.params = params;
    }

    @Override
    public ObjectNode execute() {
        if (!userExists()) {
            return error("createMilestone",
                    "The user " + username + " does not exist.");
        }

        if (!hasRole("MANAGER")) {
            return error("createMilestone",
                    "The user does not have permission to execute this command: " +
                            "required role MANAGER; user role " + userService.getRole(username) + ".");
        }

        if (flowService.isTestingPhase(timestamp)) {
            return error("createMilestone",
                    "Milestones cannot be created during testing phase.");
        }

        return milestoneService.createMilestone(username, timestamp, name, dueDate, params);
    }
}
