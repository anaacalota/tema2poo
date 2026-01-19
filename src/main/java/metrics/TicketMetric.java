package metrics;

import entities.tickets.Ticket;

public interface TicketMetric {
    /**
     * Computes the base score for a ticket based on its type
     *
     * @param ticket the ticket to calculate the base score for
     * @return the base score
     */
    double computeBaseScore(Ticket ticket);

    /**
     * Returns the maximum possible score for a ticket based on its type
     *
     * @param ticket the ticket to calculate the maximum score for
     * @return the maximum score
     */
    double getMaxValue(Ticket ticket);
}
