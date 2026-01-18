package commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface Command {
    /**
     * Executes the command
     *
     * @return the result of the command
     */
    ObjectNode execute();
}
