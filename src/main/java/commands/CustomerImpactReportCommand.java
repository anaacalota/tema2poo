package commands;

import app.services.ReportService;
import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public class CustomerImpactReportCommand implements Command {
    private final String username;
    private final String timestamp;
    private final ReportService reportService;

    public CustomerImpactReportCommand(String username, String timestamp,
                                       ReportService reportService) {
        this.username = username;
        this.timestamp = timestamp;
        this.reportService = reportService;
    }

    @Override
    public ObjectNode execute() {
        ObjectNode result = ResultUtils.createBaseNode("generateCustomerImpactReport", username, timestamp);

        Map<String, Object> report = reportService
                .generateCustomerImpactReport(username, timestamp);

        result.set("report", ResultUtils.MAPPER.valueToTree(report));

        return result;
    }
}
