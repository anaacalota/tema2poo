package commands;

import app.services.MilestoneService;
import app.services.UserService;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ViewMilestonesCommand extends BaseCommand {

    private final MilestoneService milestoneService;

    public ViewMilestonesCommand(final UserService userService,
                                 final MilestoneService milestoneService,
                                 final String username,
                                 final String timestamp) {
        super(userService, username, timestamp);
        this.milestoneService = milestoneService;
    }

    /**
     * View all milestones for a user
     *
     * @return an ObjectNode containing the command name, username, and timestamp,
     */
    @Override
    public ObjectNode execute() {

        if (!userExists()) {
            return error("viewMilestones",
                    "The user " + username + " does not exist.");
        }

        if (!hasRole("MANAGER", "DEVELOPER")) {
            return error("viewMilestones",
                    "The user does not have permission to execute this command: "
                            + "required role MANAGER, DEVELOPER; user role "
                            + userService.getRole(username) + ".");
        }

        return milestoneService.viewMilestones(username, timestamp);
    }
}
