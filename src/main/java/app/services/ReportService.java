package app.services;

import app.AppContext;
import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.tickets.Ticket;
import entities.tickets.enums.Priority;
import entities.tickets.enums.Status;
import entities.users.Developer;
import entities.users.Manager;
import entities.users.User;
import metrics.AbstractMetric;
import metrics.MetricEngine;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportService {
    private static final int RISK_THRESHOLD_NEGLIGIBLE = 25;
    private static final int RISK_THRESHOLD_MODERATE = 50;
    private static final int RISK_THRESHOLD_SIGNIFICANT = 75;
    private static final int IMPACT_THRESHOLD_STABLE = 50;
    private static final int BONUS_JUNIOR = 5;
    private static final int BONUS_MID = 15;
    private static final int BONUS_SENIOR = 30;
    private static final double WEIGHT_CLOSED_TICKETS = 0.5;
    private static final double WEIGHT_HIGH_PRIORITY_MID = 0.7;
    private static final double WEIGHT_RESOLUTION_MID = 0.3;
    private static final double WEIGHT_HIGH_PRIORITY_SENIOR = 1.0;
    private static final double WEIGHT_RESOLUTION_SENIOR = 0.5;
    private static final double ROUND_FACTOR = 100.0;
    private static final double TICKET_TYPE_COUNT = 3.0;

    private final AppContext context;
    private final MetricEngine metricEngine;

    public ReportService() {
        this.context = AppContext.getInstance();
        this.metricEngine = new MetricEngine();
    }

    /**
     * Generates a report measuring the impact of active tickets on customers
     *
     * @return a map containing total ticket counts, counts by type/priority,
     * and average impact scores
     */
    public Map<String, Object> generateCustomerImpactReport() {
        AbstractMetric metric = (AbstractMetric) metricEngine.getMetric("customerImpact");

        List<Ticket> tickets = context.getAllTickets().stream()
                .filter(ticket -> ticket.getStatus() == Status.OPEN
                        || ticket.getStatus() == Status.IN_PROGRESS)
                .toList();

        Map<String, Object> report = new LinkedHashMap<>();

        report.put("totalTickets", tickets.size());
        report.put("ticketsByType", countByType(tickets));
        report.put("ticketsByPriority", countByPriority(tickets));

        Map<String, Double> impactByType = new LinkedHashMap<>();

        for (String type : List.of("BUG", "FEATURE_REQUEST", "UI_FEEDBACK")) {

            List<Ticket> filtered = tickets.stream()
                    .filter(ticket -> ticket.getType().equals(type))
                    .toList();

            List<Double> scores = filtered.stream()
                    .map(ticket -> metric.calculateImpactFinal(
                            metric.computeBaseScore(ticket), ticket))
                    .toList();

            impactByType.put(type, round(metric.calculateAverageImpact(scores)));
        }

        report.put("customerImpactByType", impactByType);

        return report;
    }

    /**
     * Generates a report based on ticket risk levels
     *
     * @return a map containing ticket statistics and risk levels per type
     */
    public Map<String, Object> generateTicketRiskReport() {
        AbstractMetric metric = (AbstractMetric) metricEngine.getMetric("ticketRisk");

        List<Ticket> tickets = context.getAllTickets().stream()
                .filter(ticket -> ticket.getStatus() == Status.OPEN
                        || ticket.getStatus() == Status.IN_PROGRESS)
                .toList();

        Map<String, Object> report = new LinkedHashMap<>();

        report.put("totalTickets", tickets.size());
        report.put("ticketsByType", countByType(tickets));
        report.put("ticketsByPriority", countByPriority(tickets));

        Map<String, String> riskByType = new LinkedHashMap<>();

        for (String type : List.of("BUG", "FEATURE_REQUEST", "UI_FEEDBACK")) {

            List<Ticket> filtered = tickets.stream()
                    .filter(ticket -> ticket.getType().equals(type))
                    .toList();

            List<Double> scores = filtered.stream()
                    .map(ticket -> metric.calculateImpactFinal(
                            metric.computeBaseScore(ticket), ticket))
                    .toList();

            double avg = metric.calculateAverageImpact(scores);

            riskByType.put(type, classifyRisk(avg));
        }

        report.put("riskByType", riskByType);

        return report;
    }

    /**
     * Evaluates how efficiently the team resolves tickets based on completed tasks
     *
     * @return a map containing resolution statistics and efficiency scores per ticket type
     */
    public Map<String, Object> generateResolutionEfficiencyReport() {
        AbstractMetric metric = (AbstractMetric) metricEngine.getMetric("resolutionEfficiency");

        List<Ticket> tickets = context.getAllTickets().stream()
                .filter(ticket -> ticket.getStatus() == Status.RESOLVED
                        || ticket.getStatus() == Status.CLOSED)
                .toList();

        Map<String, Object> report = new LinkedHashMap<>();

        report.put("totalTickets", tickets.size());
        report.put("ticketsByType", countByType(tickets));
        report.put("ticketsByPriority", countByPriority(tickets));

        Map<String, Double> efficiencyByType = new LinkedHashMap<>();

        for (String type : List.of("BUG", "FEATURE_REQUEST", "UI_FEEDBACK")) {

            List<Ticket> filtered = tickets.stream()
                    .filter(ticket -> ticket.getType().equals(type))
                    .toList();

            List<Double> scores = filtered.stream()
                    .map(ticket -> metric.calculateImpactFinal(
                            metric.computeBaseScore(ticket), ticket))
                    .toList();

            efficiencyByType.put(type, round(metric.calculateAverageImpact(scores)));
        }

        report.put("efficiencyByType", efficiencyByType);

        return report;
    }

    /**
     * Counts the number of tickets belonging to each specific type
     *
     * @param tickets the list of tickets to analyze
     * @return a map with ticket types as keys and their occurrences as values
     */
    private Map<String, Integer> countByType(final List<Ticket> tickets) {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("BUG", 0);
        map.put("FEATURE_REQUEST", 0);
        map.put("UI_FEEDBACK", 0);

        for (Ticket ticket : tickets) {
            map.put(ticket.getType(), map.get(ticket.getType()) + 1);
        }
        return map;
    }

    /**
     * Counts the number of tickets grouped by their business priority level
     *
     * @param tickets the list of tickets to analyze
     * @return a map with priority names as keys and their occurrences as values
     */
    private Map<String, Integer> countByPriority(final List<Ticket> tickets) {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("LOW", 0);
        map.put("MEDIUM", 0);
        map.put("HIGH", 0);
        map.put("CRITICAL", 0);

        for (Ticket ticket : tickets) {
            map.put(ticket.getBusinessPriority().name(),
                    map.get(ticket.getBusinessPriority().name()) + 1);
        }
        return map;
    }

    /**
     * Classifies a risk score into one of the following labels:
     * NEGLIGIBLE, MODERATE, SIGNIFICANT, or MAJOR
     *
     * @param score the calculated risk score
     * @return a string label: NEGLIGIBLE, MODERATE, SIGNIFICANT, or MAJOR
     */
    private String classifyRisk(final double score) {
        if (score < RISK_THRESHOLD_NEGLIGIBLE) {
            return "NEGLIGIBLE";
        }
        if (score < RISK_THRESHOLD_MODERATE) {
            return "MODERATE";
        }
        if (score < RISK_THRESHOLD_SIGNIFICANT) {
            return "SIGNIFICANT";
        }
        return "MAJOR";
    }

    /**
     * Rounds a double value to two decimal places
     *
     * @param number the initial value
     * @return the rounded value
     */
    private double round(final double number) {
        return Math.round(number * ROUND_FACTOR) / ROUND_FACTOR;
    }

    /**
     * Generates a stability report for the application
     * Determines if the app is STABLE, UNSTABLE, or PARTIALLY STABLE based on risk and impact
     *
     * @param username  the manager requesting the report
     * @param timestamp the current system timestamp
     * @return an ObjectNode containing the detailed stability report
     */
    public ObjectNode generateAppStabilityReport(final String username, final String timestamp) {
        ObjectNode result = ResultUtils.createBaseNode("appStabilityReport", username, timestamp);
        AbstractMetric riskMetric = (AbstractMetric) metricEngine.getMetric("ticketRisk");
        AbstractMetric impactMetric = (AbstractMetric) metricEngine.getMetric("customerImpact");

        List<Ticket> openTickets = context.getAllTickets().stream()
                .filter(ticket -> ticket.getStatus() == Status.OPEN
                        || ticket.getStatus() == Status.IN_PROGRESS)
                .toList();

        Map<String, Object> report = new LinkedHashMap<>();

        report.put("totalOpenTickets", openTickets.size());

        if (openTickets.isEmpty()) {
            report.put("appStability", "STABLE");
        }

        report.put("openTicketsByType", countByType(openTickets));

        report.put("openTicketsByPriority", countByPriority(openTickets));

        Map<String, String> riskByType = new LinkedHashMap<>();
        Map<String, Double> impactByType = new LinkedHashMap<>();

        boolean hasSignificantRisk = false;
        boolean allNegligibleRisk = true;
        boolean allImpactBelow50 = true;

        for (String type : List.of("BUG", "FEATURE_REQUEST", "UI_FEEDBACK")) {

            List<Ticket> filtered = openTickets.stream()
                    .filter(ticket -> ticket.getType().equals(type))
                    .toList();

            // calculate risk scores
            List<Double> riskScores = filtered.stream()
                    .map(ticket -> riskMetric.calculateImpactFinal(
                            riskMetric.computeBaseScore(ticket), ticket))
                    .toList();

            double avgRisk = riskMetric.calculateAverageImpact(riskScores);
            String riskLevel = classifyRisk(avgRisk);
            riskByType.put(type, riskLevel);

            if (riskLevel.equals("SIGNIFICANT")) {
                hasSignificantRisk = true;
            }

            if (!riskLevel.equals("NEGLIGIBLE")) {
                allNegligibleRisk = false;
            }

            List<Double> impactScores = filtered.stream()
                    .map(ticket -> impactMetric.calculateImpactFinal(
                            impactMetric.computeBaseScore(ticket), ticket))
                    .toList();

            double avgImpact = round(impactMetric.calculateAverageImpact(impactScores));
            impactByType.put(type, avgImpact);

            if (avgImpact >= IMPACT_THRESHOLD_STABLE) {
                allImpactBelow50 = false;
            }
        }

        report.put("riskByType", riskByType);
        report.put("impactByType", impactByType);

        String stability;

        if (allNegligibleRisk && allImpactBelow50) {
            stability = "STABLE";
        } else if (hasSignificantRisk) {
            stability = "UNSTABLE";
        } else {
            stability = "PARTIALLY STABLE";
        }

        report.put("appStability", stability);

        result.set("report", ResultUtils.MAPPER.valueToTree(report));


        return result;
    }

    /**
     * Generates performance scores for all developers under a manager for the previous month
     *
     * @param username  the manager requesting the report
     * @param timestamp the current system timestamp
     * @return a list of maps, each representing a developer's monthly performance statistics
     */
    public List<Map<String, Object>> generatePerformanceReport(final String username,
                                                               final  String timestamp) {
        User user = context.getUser(username);
        Manager manager = (Manager) user;
        List<String> subordinates = manager.getSubordinates();

        // previous month
        LocalDate now = LocalDate.parse(timestamp);
        LocalDate firstDayPrevMonth = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayPrevMonth = now.minusMonths(1)
                .withDayOfMonth(now.minusMonths(1).lengthOfMonth());

        List<Map<String, Object>> result = new ArrayList<>();

        for (String devName : subordinates) {

            User userDeveloper = context.getUser(devName);
            Developer dev = (Developer) userDeveloper;

            List<Ticket> closed = context.getAllTickets().stream()
                    .filter(ticket -> ticket.getAssignedTo()
                            != null && ticket.getAssignedTo().equals(devName))
                    .filter(ticket -> ticket.getStatus() == Status.CLOSED)
                    .filter(ticket -> {
                        LocalDate solved = ticket.getSolvedAt();
                        System.out.println("Ticket " + ticket.getId()
                                + " solved at " + ticket.getSolvedAt());
                        return !solved.isBefore(firstDayPrevMonth)
                                && !solved.isAfter(lastDayPrevMonth);
                    })
                    .toList();

            int closedTickets = closed.size();
            if (closedTickets == 0) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("username", devName);
                entry.put("closedTickets", 0);
                entry.put("averageResolutionTime", 0.0);
                entry.put("performanceScore", 0.0);
                entry.put("seniority", dev.getSeniority().name());
                result.add(entry);
                continue;
            }

            double avgResolution = 0.0;
            if (closedTickets > 0) {
                avgResolution = closed.stream()
                        .mapToDouble(ticket -> {
                            LocalDate assigned = ticket.getAssignedAt();
                            LocalDate solved = ticket.getSolvedAt();
                            return ChronoUnit.DAYS.between(assigned, solved) + 1;
                        })
                        .average()
                        .orElse(0.0);
            }

            // high priority tickets
            long highPriority = closed.stream()
                    .filter(ticket -> ticket.getBusinessPriority() == Priority.HIGH
                            || ticket.getBusinessPriority() == Priority.CRITICAL)
                    .count();

            // ticket type counts
            int bug = (int) closed.stream().filter(ticket
                    -> ticket.getType().equals("BUG")).count();
            int feature = (int) closed.stream().filter(ticket
                    -> ticket.getType().equals("FEATURE_REQUEST")).count();
            int ui = (int) closed.stream().filter(ticket
                    -> ticket.getType().equals("UI_FEEDBACK")).count();

            // calculate performance score
            double score = 0.0;
            String seniority = dev.getSeniority().name();

            switch (dev.getSeniority()) {

                case JUNIOR -> {
                    double diversity = getDiversity(bug, feature, ui);

                    score = Math.max(0, WEIGHT_CLOSED_TICKETS * closedTickets
                            - diversity) + BONUS_JUNIOR;
                }

                case MID -> score = Math.max(0,
                        WEIGHT_CLOSED_TICKETS * closedTickets
                                + WEIGHT_HIGH_PRIORITY_MID * highPriority
                                - WEIGHT_RESOLUTION_MID * avgResolution
                ) + BONUS_MID;

                case SENIOR -> score = Math.max(0,
                        WEIGHT_CLOSED_TICKETS * closedTickets
                                + WEIGHT_HIGH_PRIORITY_SENIOR * highPriority
                                - WEIGHT_RESOLUTION_SENIOR * avgResolution
                ) + BONUS_SENIOR;
                default -> {
                }
            }

            // round
            double avgResRounded = round(avgResolution);
            double scoreRounded = round(score);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("username", devName);
            entry.put("closedTickets", closedTickets);
            entry.put("averageResolutionTime", avgResRounded);
            entry.put("performanceScore", scoreRounded);
            entry.put("seniority", seniority);

            result.add(entry);
        }

        // sort by name
        result.sort(Comparator.comparing(e -> (String) e.get("username")));

        return result;
    }

    private static double getDiversity(final int bug, final int feature, final int ui) {
        double avgType = (bug + feature + ui) / TICKET_TYPE_COUNT;
        double std = 0.0;
        if (avgType != 0) {
            double variance = (Math.pow(bug - avgType, 2)
                    + Math.pow(feature - avgType, 2)
                    + Math.pow(ui - avgType, 2)) / TICKET_TYPE_COUNT;
            std = Math.sqrt(variance);
        }
        double diversity;
        if (avgType == 0) {
            diversity = 0;
        } else {
            diversity = std / avgType;
        }
        return diversity;
    }

}
