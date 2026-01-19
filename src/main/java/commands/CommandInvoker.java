package commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class CommandInvoker {
    /**
     * Invokes a command
     *
     * @param command - the command to invoke
     * @return the result of the command
     */
    public ObjectNode invoke(final Command command) {
        return command.execute();
    }
}
