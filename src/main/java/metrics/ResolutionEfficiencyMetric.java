package metrics;


import entities.tickets.BugTicket;
import entities.tickets.FeatureRequestTicket;
import entities.tickets.Ticket;
import entities.tickets.UiFeedbackTicket;

public class ResolutionEfficiencyMetric extends AbstractMetric {
    private static final double BUG_SCALING_FACTOR = 10.0;
    private static final int MAX_BUG_VALUE = 70;
    private static final int MAX_FEATURE_REQUEST_VALUE = 20;
    private static final int MAX_UI_FEEDBACK_VALUE = 20;

    /**
     * Calculates the base score for a ticket based on its type
     *
     * @param ticket the ticket to calculate the base score for
     * @return the base score
     */
    @Override
    public double computeBaseScore(final Ticket ticket) {
        int days = ticket.daysToResolve();
        if (days <= 0) {
            return 0;
        }

        double result;
        switch (ticket.getType()) {
            case "BUG" -> {
                BugTicket bug = (BugTicket) ticket;
                result = (bug.getFrequency().getValue()
                        + bug.getSeverity().getValue())
                        * BUG_SCALING_FACTOR / days;
            }

            case "FEATURE_REQUEST" -> {
                FeatureRequestTicket featureRequest = (FeatureRequestTicket) ticket;
                result = (featureRequest.getBusinessValue().getValue()
                        + featureRequest.getCustomerDemand().getValue())
                        / (double) days;
            }

            case "UI_FEEDBACK" -> {
                UiFeedbackTicket ticketUi = (UiFeedbackTicket) ticket;
                result = (ticketUi.getUsabilityScore()
                        + ticketUi.getBusinessValue().getValue())
                        / (double) days;
            }
            default -> result = 0;
        }
        return result;
    }

    /**
     * Returns the maximum possible score for a ticket based on its type
     *
     * @param ticket the ticket to calculate the maximum score for
     * @return the maximum score
     */
    @Override
    public double getMaxValue(final Ticket ticket) {
        return switch (ticket.getType()) {
            case "BUG" -> MAX_BUG_VALUE;
            case "FEATURE_REQUEST" -> MAX_FEATURE_REQUEST_VALUE;
            case "UI_FEEDBACK" -> MAX_UI_FEEDBACK_VALUE;
            default -> 1;
        };
    }
}

