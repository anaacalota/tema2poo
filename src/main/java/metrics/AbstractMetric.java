package metrics;

import entities.tickets.Ticket;

import java.util.List;

public abstract class AbstractMetric implements TicketMetric {
    private static final double SCALE_FACTOR = 100.0;

    /**
     * Returns the maximum score for a ticket based on its type
     *
     * @param ticket the ticket to calculate the maximum score for
     * @return the maximum score
     */
    public abstract double getMaxValue(Ticket ticket);

    /**
     * Calculates the final impact score for a ticket
     *
     * @param baseScore the base score obtained from the metric
     * @param ticket the ticket to calculate the impact for
     * @return the final impact score
     */
    public double calculateImpactFinal(final double baseScore, final Ticket ticket) {
        double maxValue = getMaxValue(ticket);
        return Math.min(SCALE_FACTOR, (baseScore * SCALE_FACTOR) / maxValue);
    }

    /**
     * Calculates the average impact score from a list of scores
     *
     * @param scores the list of scores to average
     * @return the average impact score
     */
    public double calculateAverageImpact(final List<Double> scores) {
        return scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}
