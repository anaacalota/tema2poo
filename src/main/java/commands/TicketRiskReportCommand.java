package commands;

import app.AppContext;
import app.services.ReportService;
import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public class TicketRiskReportCommand implements Command {

    private final String username;
    private final String timestamp;
    private final ReportService reportService;

    public TicketRiskReportCommand(String username, String timestamp, ReportService reportService) {
        this.username = username;
        this.timestamp = timestamp;
        this.reportService = reportService;
    }

    @Override
    public ObjectNode execute() {

        AppContext context = AppContext.getInstance();

        ObjectNode result = ResultUtils.createBaseNode("generateTicketRiskReport", username, timestamp);

        Map<String, Object> report = reportService
                .generateTicketRiskReport(username, timestamp);

        result.set("report", ResultUtils.MAPPER.valueToTree(report));

        return result;
    }
}
