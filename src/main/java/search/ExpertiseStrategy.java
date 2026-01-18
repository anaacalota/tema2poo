package search;

import entities.users.Developer;

public class ExpertiseStrategy implements SearchStrategy<Developer> {
    private final String requiredExpertise;

    public ExpertiseStrategy(final String requiredExpertise) {
        this.requiredExpertise = requiredExpertise;
    }

    /**
     * Checks if the developer's expertise matches the required expertise area
     *
     * @param dev the developer to evaluate
     * @return true if the developer belongs to the specified expertise area
     */
    @Override
    public boolean matches(final Developer dev) {
        return dev.getExpertiseArea().name().equals(requiredExpertise);
    }
}
