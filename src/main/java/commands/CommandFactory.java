package commands;

import app.services.*;
import app.services.view.ViewTicketService;
import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;

public class CommandFactory {
    private final UserService userService;
    private final TicketService ticketService;
    private final MilestoneService milestoneService;
    private final ViewTicketService viewService;
    private final FlowService flowService;
    private final SearchService searchService;
    private final ReportService reportService;

    public CommandFactory(final UserService userService,
                          final TicketService ticketService,
                          final MilestoneService milestoneService,
                          final ViewTicketService viewService,
                          final FlowService flowService,
                          final SearchService searchService,
                          final ReportService reportService) {
        this.userService = userService;
        this.ticketService = ticketService;
        this.milestoneService = milestoneService;
        this.viewService = viewService;
        this.flowService = flowService;
        this.searchService = searchService;
        this.reportService = reportService;
    }

    /**
     * Creates a command based on the command name and parameters
     *
     * @param commandInput the input containing the command name and parameters
     * @return the created command
     */
    public Command create(CommandInput commandInput) {
        String username = commandInput.getUsername();
        String timestamp = commandInput.getTimestamp();
        String commandName = commandInput.getCommand();
        ObjectNode commandNode = ResultUtils.MAPPER.valueToTree(commandInput);

        // optional fields
        JsonNode params = null;
        String name = null;
        String dueDate = null;

        if (commandInput.getParams() != null) {
            params = commandInput.getParams();
        }

        if (commandInput.getName() != null) {
            name = commandInput.getName();
        }

        if (commandInput.getDueDate() != null) {
            dueDate = commandInput.getDueDate();
        }

        return switch (commandName) {
            case "reportTicket" -> new ReportTicketCommand(
                    userService,
                    ticketService,
                    username,
                    timestamp,
                    params
            );
            case "viewTickets" -> new ViewTicketsCommand(
                    userService,
                    ticketService,
                    viewService,
                    username,
                    timestamp
            );
            case "viewMilestones" -> new ViewMilestonesCommand(
                    userService,
                    milestoneService,
                    username,
                    timestamp
            );
            case "createMilestone" -> new CreateMilestoneCommand(
                    userService,
                    milestoneService,
                    flowService,
                    username,
                    timestamp,
                    name,
                    dueDate,
                    commandNode
            );
            case "assignTicket" -> new AssignTicketCommand(
                    userService,
                    ticketService,
                    username,
                    timestamp,
                    commandInput.getTicketID()
            );
            case "undoAssignTicket" -> new UndoAssignTicketCommand(
                    userService,
                    ticketService,
                    username,
                    timestamp,
                    commandInput.getTicketID()
            );
            case "viewAssignedTickets" -> new ViewAssignedTicketsCommand(
                    userService,
                    ticketService,
                    username,
                    timestamp
            );
            case "addComment" -> new AddCommentCommand(
                    userService,
                    ticketService,
                    username,
                    timestamp,
                    commandInput.getTicketID(),
                    commandInput.getComment()
            );
            case "undoAddComment" -> new UndoAddCommentCommand(
                    userService,
                    ticketService,
                    username,
                    timestamp,
                    commandInput.getTicketID()
            );
            case "changeStatus" -> new ChangeStatusCommand(
                    username,
                    timestamp,
                    commandInput.getTicketID()
            );
            case "undoChangeStatus" -> new UndoChangeStatusCommand(
                    username,
                    timestamp,
                    commandInput.getTicketID()
            );
            case "viewTicketHistory" -> new ViewTicketHistoryCommand(
                    userService,
                    ticketService,
                    username,
                    timestamp
            );
            case "search" -> new SearchCommand(
                    userService,
                    searchService,
                    username,
                    timestamp,
                    commandInput.getFilters()
            );
            case "viewNotifications" -> new ViewNotificationsCommand(
                    userService,
                    username,
                    timestamp
            );
            case "generateCustomerImpactReport" -> new CustomerImpactReportCommand(
                    username,
                    timestamp,
                    reportService
            );
            case "generateTicketRiskReport" -> new TicketRiskReportCommand(
                    username,
                    timestamp,
                    reportService
            );
            case "generateResolutionEfficiencyReport" -> new ResolutionEfficiencyReportCommand(
                    username,
                    timestamp,
                    reportService
            );
            case "appStabilityReport" -> new AppStabilityReportCommand(
                    username,
                    timestamp,
                    reportService
            );
            case "generatePerformanceReport" -> new GeneratePerformanceReportCommand(
                    username,
                    timestamp,
                    reportService
            );
            case "startTestingPhase" -> new StartTestingPhaseCommand(
                    username,
                    timestamp
            );
            case "lostInvestors" -> new LostInvestorsCommand(
                    userService,
                    flowService,
                    username,
                    timestamp
            );
            default -> null;
        };
    }
}
