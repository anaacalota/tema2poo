package commands;

import app.AppContext;
import app.services.FlowService;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class StartTestingPhaseCommand implements Command {
    private final String timestamp;

    public StartTestingPhaseCommand(final String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Start a new testing phase
     *
     * @return null
     */
    @Override
    public ObjectNode execute() {
        AppContext context = AppContext.getInstance();
        FlowService flowService = new FlowService();
        flowService.startNewTestingPhase(timestamp);
        context.setTestingPhaseActive(true);

        return null;
    }
}