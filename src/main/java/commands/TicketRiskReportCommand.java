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

    public TicketRiskReportCommand(final String username, final String timestamp,
                                   final ReportService reportService) {
        this.username = username;
        this.timestamp = timestamp;
        this.reportService = reportService;
    }

    /**
     * Generate a ticket risk report
     *
     * @return an ObjectNode containing the report
     */
    @Override
    public ObjectNode execute() {
        ObjectNode result = ResultUtils.createBaseNode("generateTicketRiskReport", username, timestamp);

        Map<String, Object> report = reportService.generateTicketRiskReport();

        result.set("report", ResultUtils.MAPPER.valueToTree(report));

        return result;
    }
}
