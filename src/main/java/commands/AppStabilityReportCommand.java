package commands;

import app.services.ReportService;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AppStabilityReportCommand implements Command {
    private final String username;
    private final String timestamp;
    private final ReportService reportService;

    public AppStabilityReportCommand(final String username,
                                     final String timestamp,
                                     final ReportService reportService) {
        this.username = username;
        this.timestamp = timestamp;
        this.reportService = reportService;
    }

    /**
     * Generate an app stability report for a given user and timestamp
     *
     * @return an ObjectNode containing the report
     */
    @Override
    public ObjectNode execute() {
        return reportService.generateAppStabilityReport(username, timestamp);
    }
}
