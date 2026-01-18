package search;

import entities.users.Developer;

public class PerformanceScoreBelowStrategy implements SearchStrategy<Developer> {
    private final double limit;

    public PerformanceScoreBelowStrategy(final double limit) {
        this.limit = limit;
    }

    /**
     * Checks if the developer's performance score is lower than the threshold
     *
     * @param dev the developer to evaluate
     * @return true if the developer's score is below the limit
     */
    @Override
    public boolean matches(final Developer dev) {
        return dev.getPerformanceScore() < limit;
    }
}
