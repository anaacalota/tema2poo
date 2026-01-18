package commands;

import app.services.UserService;
import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class BaseCommand implements Command {
    protected final UserService userService;
    protected final String username;
    protected final String timestamp;
    protected final ObjectMapper mapper = ResultUtils.MAPPER;

    protected BaseCommand(final UserService userService,
                          final String username,
                          final String timestamp) {
        this.userService = userService;
        this.username = username;
        this.timestamp = timestamp;
    }

    /**
     * Create an error ObjectNode with the given command name and message
     *
     * @param commandName the name of the command that failed
     * @param message the error message
     * @return an ObjectNode containing the command name, username, and timestamp, as well as the error message
     */
    protected ObjectNode error(final String commandName, final String message) {
        ObjectNode node = ResultUtils.createBaseNode(commandName, username, timestamp);
        node.put("error", message);
        return node;
    }

    /**
     * Create a success ObjectNode with the given command name
     *
     * @param commandName the name of the command that succeeded
     * @return an ObjectNode containing the command name, username, and timestamp
     */
    protected ObjectNode success(final String commandName) {
        ObjectNode node = mapper.createObjectNode();
        node.put("command", commandName);
        node.put("username", username);
        node.put("timestamp", timestamp);
        return node;
    }

    /**
     * Check if the user exists in the database
     *
     * @return true if the user exists, false otherwise
     */
    protected boolean userExists() {
        return userService.userExists(username);
    }

    /**
     * Check if the user has one of the specified roles
     * @param roles the roles to check for
     * @return true if the user has one of the specified roles, false otherwise
     */
    protected boolean hasRole(final String... roles) {
        return userService.hasRole(username, roles);
    }
}
