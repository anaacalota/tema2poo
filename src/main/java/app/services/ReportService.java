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
import java.util.*;

public class ReportService {

    private final AppContext context;
    private final MetricEngine metricEngine;

    public ReportService() {
        this.context = AppContext.getInstance();
        this.metricEngine = new MetricEngine();
    }

    public Map<String, Object> generateCustomerImpactReport(String username, String timestamp) {

        AbstractMetric metric = (AbstractMetric) metricEngine.getMetric("customerImpact");

        List<Ticket> tickets = context.getAllTickets().stream()
                .filter(ticket -> ticket.getStatus() == Status.OPEN || ticket.getStatus() == Status.IN_PROGRESS)
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
                    .map(ticket -> metric.calculateImpactFinal(metric.computeBaseScore(ticket), ticket))
                    .toList();

            impactByType.put(type, round(metric.calculateAverageImpact(scores)));
        }

        report.put("customerImpactByType", impactByType);

        return report;
    }

    public Map<String, Object> generateTicketRiskReport(String username, String timestamp) {

        AbstractMetric metric = (AbstractMetric) metricEngine.getMetric("ticketRisk");

        List<Ticket> tickets = context.getAllTickets().stream()
                .filter(ticket -> ticket.getStatus() == Status.OPEN || ticket.getStatus() == Status.IN_PROGRESS)
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
                    .map(ticket -> metric.calculateImpactFinal(metric.computeBaseScore(ticket), ticket))
                    .toList();

            double avg = metric.calculateAverageImpact(scores);

            riskByType.put(type, classifyRisk(avg));
        }

        report.put("riskByType", riskByType);

        return report;
    }

    public Map<String, Object> generateResolutionEfficiencyReport(String username, String timestamp) {

        AbstractMetric metric = (AbstractMetric) metricEngine.getMetric("resolutionEfficiency");

        List<Ticket> tickets = context.getAllTickets().stream()
                .filter(ticket -> ticket.getStatus() == Status.RESOLVED || ticket.getStatus() == Status.CLOSED)
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
                    .map(ticket -> metric.calculateImpactFinal(metric.computeBaseScore(ticket), ticket))
                    .toList();

            efficiencyByType.put(type, round(metric.calculateAverageImpact(scores)));
        }

        report.put("efficiencyByType", efficiencyByType);

        return report;
    }

    private Map<String, Integer> countByType(List<Ticket> tickets) {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("BUG", 0);
        map.put("FEATURE_REQUEST", 0);
        map.put("UI_FEEDBACK", 0);

        for (Ticket ticket : tickets) {
            map.put(ticket.getType(), map.get(ticket.getType()) + 1);
        }
        return map;
    }

    private Map<String, Integer> countByPriority(List<Ticket> tickets) {
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

    private String classifyRisk(double score) {
        if (score < 25) return "NEGLIGIBLE";
        if (score < 50) return "MODERATE";
        if (score < 75) return "SIGNIFICANT";
        return "MAJOR";
    }

    private double round(double number) {
        return Math.round(number * 100.0) / 100.0;
    }

    public ObjectNode generateAppStabilityReport(String username, String timestamp) {
        ObjectNode result = ResultUtils.createBaseNode("appStabilityReport", username, timestamp);
        AbstractMetric riskMetric = (AbstractMetric) metricEngine.getMetric("ticketRisk");
        AbstractMetric impactMetric = (AbstractMetric) metricEngine.getMetric("customerImpact");

        List<Ticket> openTickets = context.getAllTickets().stream()
                .filter(ticket -> ticket.getStatus() == Status.OPEN || ticket.getStatus() == Status.IN_PROGRESS)
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
                    .map(ticket -> riskMetric.calculateImpactFinal(riskMetric.computeBaseScore(ticket), ticket))
                    .toList();

            double avgRisk = riskMetric.calculateAverageImpact(riskScores);
            String riskLevel = classifyRisk(avgRisk);
            riskByType.put(type, riskLevel);

            if (riskLevel.equals("SIGNIFICANT"))
                hasSignificantRisk = true;

            if (!riskLevel.equals("NEGLIGIBLE"))
                allNegligibleRisk = false;

            List<Double> impactScores = filtered.stream()
                    .map(ticket -> impactMetric.calculateImpactFinal(impactMetric.computeBaseScore(ticket), ticket))
                    .toList();

            double avgImpact = round(impactMetric.calculateAverageImpact(impactScores));
            impactByType.put(type, avgImpact);

            if (avgImpact >= 50)
                allImpactBelow50 = false;
        }

        report.put("riskByType", riskByType);
        report.put("impactByType", impactByType);

        String stability;

        if (allNegligibleRisk && allImpactBelow50) {
            stability = "STABLE";
        } else if (hasSignificantRisk) {
            stability = "UNSTABLE";
        } else {
            stability = "PARTIALLY_STABLE";
        }

        report.put("appStability", stability);

        result.set("report", ResultUtils.MAPPER.valueToTree(report));


        return result;
    }

    public List<Map<String, Object>> generatePerformanceReport(String username, String timestamp) {

        User user = context.getUser(username);
        Manager manager = (Manager) user;
        List<String> subordinates = manager.getSubordinates();

        // previous month
        LocalDate now = LocalDate.parse(timestamp);
        LocalDate firstDayPrevMonth = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayPrevMonth = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());

        List<Map<String, Object>> result = new ArrayList<>();

        for (String devName : subordinates) {

            User userDeveloper = context.getUser(devName);
            Developer dev = (Developer) userDeveloper;

            List<Ticket> closed = context.getAllTickets().stream()
                    .filter(ticket -> ticket.getAssignedTo() != null && ticket.getAssignedTo().equals(devName))
                    .filter(ticket -> ticket.getStatus() == Status.CLOSED)
                    .filter(ticket -> {
                        LocalDate solved = ticket.getSolvedAt();
                        System.out.println("Ticket " + ticket.getId() + " solved at " + ticket.getSolvedAt());
                        return !solved.isBefore(firstDayPrevMonth) && !solved.isAfter(lastDayPrevMonth);
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
                    .filter(ticket -> ticket.getBusinessPriority() == Priority.HIGH ||
                            ticket.getBusinessPriority() == Priority.CRITICAL)
                    .count();

            // ticket type counts
            int bug = (int) closed.stream().filter(ticket -> ticket.getType().equals("BUG")).count();
            int feature = (int) closed.stream().filter(ticket -> ticket.getType().equals("FEATURE_REQUEST")).count();
            int ui = (int) closed.stream().filter(ticket -> ticket.getType().equals("UI_FEEDBACK")).count();

            // calculate performance score
            double score = 0.0;
            String seniority = dev.getSeniority().name();

            switch (dev.getSeniority()) {

                case JUNIOR -> {
                    double avgType = (bug + feature + ui) / 3.0;
                    double std = 0.0;
                    if (avgType != 0) {
                        double variance = (Math.pow(bug - avgType, 2) +
                                Math.pow(feature - avgType, 2) +
                                Math.pow(ui - avgType, 2)) / 3.0;
                        std = Math.sqrt(variance);
                    }
                    double diversity;
                    if (avgType == 0) {
                        diversity = 0;
                    } else {
                        diversity = std / avgType;
                    }

                    score = Math.max(0, 0.5 * closedTickets - diversity) + 5;
                }

                case MID -> {
                    score = Math.max(0,
                            0.5 * closedTickets +
                                    0.7 * highPriority -
                                    0.3 * avgResolution
                    ) + 15;
                }

                case SENIOR -> {
                    score = Math.max(0,
                            0.5 * closedTickets +
                                    1.0 * highPriority -
                                    0.5 * avgResolution
                    ) + 30;
                }
            }

            // round
            double avgResRounded = Math.round(avgResolution * 100.0) / 100.0;
            double scoreRounded = Math.round(score * 100.0) / 100.0;

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




}
