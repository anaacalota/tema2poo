package commands;

import app.AppContext;
import app.services.UserService;
import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class ViewNotificationsCommand extends BaseCommand {

    public ViewNotificationsCommand(UserService userService,
                                    String username,
                                    String timestamp) {
        super(userService, username, timestamp);
    }

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
