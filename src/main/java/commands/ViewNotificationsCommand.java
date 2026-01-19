package commands;

import app.AppContext;
import app.services.UserService;
import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class ViewNotificationsCommand extends BaseCommand {
    public ViewNotificationsCommand(final UserService userService,
                                    final String username,
                                    final String timestamp) {
        super(userService, username, timestamp);
    }

    /**
     * View all notifications for a user
     *
     * @return an ObjectNode containing the command name, username, and timestamp,
     */
    @Override
    public ObjectNode execute() {
        AppContext context = AppContext.getInstance();
        ObjectNode result = ResultUtils.createBaseNode("viewNotifications", username, timestamp);

        List<String> list = context.getNotifications(username);
        result.set("notifications", ResultUtils.MAPPER.valueToTree(list));

        context.clearNotifications(username);

        return result;
    }
}
