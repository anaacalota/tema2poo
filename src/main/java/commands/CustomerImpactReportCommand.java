package commands;

import app.services.ReportService;
import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public class CustomerImpactReportCommand implements Command {
    private final String username;
    private final String timestamp;
    private final ReportService reportService;

    public CustomerImpactReportCommand(final String username, final String timestamp,
                                       final ReportService reportService) {
        this.username = username;
        this.timestamp = timestamp;
        this.reportService = reportService;
    }

    /**
     * Generate a customer impact report
     *
     * @return an ObjectNode containing the report
     */
    @Override
    public ObjectNode execute() {
        ObjectNode result = ResultUtils.createBaseNode(
                "generateCustomerImpactReport", username, timestamp);

        Map<String, Object> report = reportService
                .generateCustomerImpactReport();

        result.set("report", ResultUtils.MAPPER.valueToTree(report));

        return result;
    }
}
