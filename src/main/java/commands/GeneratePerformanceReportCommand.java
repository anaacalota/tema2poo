package commands;

import app.services.ReportService;
import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

public class GeneratePerformanceReportCommand implements Command {
    private final String username;
    private final String timestamp;
    private final ReportService reportService;

    public GeneratePerformanceReportCommand(String username, String timestamp,
                                            ReportService reportService) {
        this.username = username;
        this.timestamp = timestamp;
        this.reportService = reportService;
    }

    @Override
    public ObjectNode execute() {
        ObjectNode result = ResultUtils.createBaseNode("generatePerformanceReport", username, timestamp);

        List<Map<String, Object>> report = reportService
                .generatePerformanceReport(username, timestamp);

        result.set("report", ResultUtils.MAPPER.valueToTree(report));

        return result;
    }
}
