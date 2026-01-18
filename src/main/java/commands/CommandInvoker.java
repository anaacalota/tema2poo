package commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class CommandInvoker {
    public ObjectNode invoke(Command command) {
        return command.execute();
    }
    
}
