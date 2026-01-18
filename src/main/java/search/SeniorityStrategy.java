package search;

import entities.users.Developer;
import entities.users.enums.Seniority;

public class SeniorityStrategy implements SearchStrategy<Developer> {
    private final Seniority level;

    public SeniorityStrategy(final Seniority level) {
        this.level = level;
    }

    /**
     * Checks if the developer's seniority matches the required level
     *
     * @param dev the developer to evaluate
     * @return true if the developer's seniority level is equal to the target level
     */
    @Override
    public boolean matches(final Developer dev) {
        return dev.getSeniority() == level;
    }
}
