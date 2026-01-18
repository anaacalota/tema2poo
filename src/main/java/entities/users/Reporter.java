package entities.users;

import entities.users.enums.UserRole;

public class Reporter extends User {
    public Reporter(final String username, final String email) {
        super(username, email, UserRole.REPORTER);
    }

    /**
     * Checks if the user is a developer
     *
     * @return false since Reporter is not a developer
     */
    @Override
    public boolean isDeveloper() {
        return false;
    }

    /**
     * Checks if the user is a manager
     *
     * @return false since Reporter is not a manager
     */
    @Override
    public boolean isManager() {
        return false;
    }

    /**
     * Update method for Observer pattern
     *
     * @param message the message to be sent to the user
     */
    @Override public void update(final String message) {

    }
}
