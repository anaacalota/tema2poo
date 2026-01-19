package main;

import app.AppContext;
import app.services.FlowService;
import app.services.MilestoneService;
import app.services.ReportService;
import app.services.SearchService;
import app.services.TicketService;
import app.services.UserService;
import app.services.view.ViewTicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import commands.CommandFactory;
import commands.CommandInvoker;
import entities.milestones.Milestone;
import entities.users.UserFactory;
import fileio.CommandInput;
import fileio.CommandsLoader;
import fileio.UserInput;
import fileio.UsersLoader;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class App {
    private App() {
    }

    private static final String INPUT_USERS_FIELD = "input/database/users.json";
    private static final ObjectWriter WRITER =
            new ObjectMapper().writer().withDefaultPrettyPrinter();

    /**
     * Runs the application: reads commands from an input file,
     * processes them, generates results, and writes them to an output file
     *
     * @param inputPath path to the input file containing commands
     * @param outputPath path to the file where results should be written
     */
    public static void run(final String inputPath, final String outputPath) throws IOException {
        List<ObjectNode> outputs = new ArrayList<>();

        AppContext context = AppContext.getInstance();
        context.reset();

        UserService userService = new UserService();
        FlowService flowService = new FlowService();
        TicketService ticketService = new TicketService(flowService);
        MilestoneService milestoneService = new MilestoneService(flowService);
        SearchService searchService = new SearchService(ticketService);
        ReportService reportService = new ReportService();
        ViewTicketService viewTicketService = new ViewTicketService(ticketService);
        CommandFactory factory = new CommandFactory(userService, ticketService,
                milestoneService, viewTicketService, flowService, searchService, reportService);
        CommandInvoker invoker = new CommandInvoker();

        // users
        for (UserInput user : UsersLoader.load(INPUT_USERS_FIELD)) {
            context.addUser(UserFactory.createUser(user));
        }

        // commands
        for (CommandInput commandInput : CommandsLoader.load(inputPath)) {
            if (flowService.investorsLost()) {
                break;
            }

            Command command = factory.create(commandInput);

            if (command == null) {
                continue;
            }

            for (Milestone milestone : context.getAllMilestones()) {
                milestoneService.updateMilestonePriorities(milestone,
                        LocalDate.parse(commandInput.getTimestamp()));
                milestoneService.updateCriticalBeforeDue(milestone,
                        LocalDate.parse(commandInput.getTimestamp()));
            }

            ObjectNode result = invoker.invoke(command);
            flowService.checkMilestonesUnblockedAfterDueDate(commandInput.getTimestamp());
            flowService.checkMilestonesDueTomorrow(commandInput.getTimestamp());

            if (result != null) {
                outputs.add(result);
            }
        }

        try {
            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();
            WRITER.withDefaultPrettyPrinter().writeValue(outputFile, outputs);
        } catch (IOException e) {
            System.out.println("error writing to output file: " + e.getMessage());
        }
    }
}
