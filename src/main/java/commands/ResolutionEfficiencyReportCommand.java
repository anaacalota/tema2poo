package commands;

import app.services.ReportService;
import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public class ResolutionEfficiencyReportCommand implements Command {
    private final String username;
    private final String timestamp;
    private final ReportService reportService;

    public ResolutionEfficiencyReportCommand(final String username, final String timestamp,
                                             final ReportService reportService) {
        this.username = username;
        this.timestamp = timestamp;
        this.reportService = reportService;
    }

    /**
     * Generate a resolution efficiency report
     *
     * @return an ObjectNode containing the report
     */
    @Override
    public ObjectNode execute() {
        ObjectNode result = ResultUtils.createBaseNode(
                "generateResolutionEfficiencyReport", username, timestamp);
        Map<String, Object> report = reportService
                .generateResolutionEfficiencyReport();
        result.set("report", ResultUtils.MAPPER.valueToTree(report));

        return result;
    }
}
