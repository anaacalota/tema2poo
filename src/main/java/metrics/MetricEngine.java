package metrics;

import java.util.HashMap;
import java.util.Map;

public class MetricEngine {
    private final Map<String, TicketMetric> metrics = new HashMap<>();

    public MetricEngine() {
        metrics.put("customerImpact", new CustomerImpactMetric());
        metrics.put("ticketRisk", new TicketRiskMetric());
        metrics.put("resolutionEfficiency", new ResolutionEfficiencyMetric());
    }

    /**
     * Returns a metric by name
     *
     * @param name the name of the metric
     * @return the metric
     */
    public TicketMetric getMetric(final String name) {
        return metrics.get(name);
    }
}
