package commands;

import app.AppContext;
import app.services.FlowService;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class StartTestingPhaseCommand implements Command {

    private final String username;
    private final String timestamp;

    public StartTestingPhaseCommand(String username, String timestamp) {
        this.username = username;
        this.timestamp = timestamp;
    }

    @Override
    public ObjectNode execute() {
        AppContext context = AppContext.getInstance();
        FlowService flowService = new FlowService();
        flowService.startNewTestingPhase(timestamp);
        context.setTestingPhaseActive(true);

        return null;
    }
}