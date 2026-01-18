package app.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class ResultUtils {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    private ResultUtils() {

    }

    /**
     * Creates a base JSON node with common fields for command results
     *
     * @param command the command name
     * @param username the username executing the command
     * @param timestamp the timestamp of the command execution
     * @return an ObjectNode with the base fields populated
     */
    public static ObjectNode createBaseNode(final String command, final String username,
                                            final String timestamp) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("command", command);
        node.put("username", username);
        node.put("timestamp", timestamp);
        return node;
    }

}
