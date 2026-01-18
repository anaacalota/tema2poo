package search;

import entities.users.Developer;

public class PerformanceScoreAboveStrategy implements SearchStrategy<Developer> {
    private final double limit;

    public PerformanceScoreAboveStrategy(final double limit) {
        this.limit = limit;
    }

    /**
     * Checks if the developer's performance score is greater than the threshold
     *
     * @param dev the developer to evaluate
     * @return true if the developer's score exceeds the limit
     */
    @Override
    public boolean matches(final Developer dev) {
        return dev.getPerformanceScore() >= limit;
    }
}
