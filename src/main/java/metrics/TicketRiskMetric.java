package metrics;

import entities.tickets.BugTicket;
import entities.tickets.FeatureRequestTicket;
import entities.tickets.Ticket;
import entities.tickets.UiFeedbackTicket;

public class TicketRiskMetric extends AbstractMetric {
    private static final int USABILITY_OFFSET = 11;
    private static final int MAX_BUG_VALUE = 12;
    private static final int MAX_FEATURE_REQUEST_VALUE = 20;
    private static final int MAX_UI_FEEDBACK_VALUE = 100;
    /**
     * Calculates the base score for a ticket based on its type
     *
     * @param ticket the ticket to calculate the base score for
     * @return the base score
     */
    @Override
    public double computeBaseScore(final Ticket ticket) {
        double result;
         switch (ticket.getType()) {
            case "BUG" -> {
                BugTicket bugTicket = (BugTicket) ticket;
                result = bugTicket.getFrequency().getValue()
                        * bugTicket.getSeverity().getValue();
            }

            case "FEATURE_REQUEST" -> {
                FeatureRequestTicket featureRequest = (FeatureRequestTicket) ticket;
                result = featureRequest.getBusinessValue().getValue()
                        + featureRequest.getCustomerDemand().getValue();
            }

            case "UI_FEEDBACK" -> {
                UiFeedbackTicket uiFeedbackTicket = (UiFeedbackTicket) ticket;
                result = (USABILITY_OFFSET - uiFeedbackTicket.getUsabilityScore())
                        * uiFeedbackTicket.getBusinessValue().getValue();
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
