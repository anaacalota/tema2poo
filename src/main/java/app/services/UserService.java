package app.services;

import app.AppContext;
import entities.users.User;

public class UserService {
    private final AppContext context;

    public UserService() {
        this.context = AppContext.getInstance();
    }

    /**
     * Checks if a user exists in the system
     *
     * @param username the username to check
     * @return true if the user exists, false otherwise
     */
    public boolean userExists(final String username) {
        return context.getUser(username) != null;
    }

    /**
     * Retrieves the role of a user by their username
     *
     * @param username the username of the user
     * @return the role of the user as a string, or an empty string if the user does not exist
     */
    public String getRole(final String username) {
        User user = context.getUser(username);
        if (user != null) {
            return user.getRole().name();
        } else {
            return "";
        }
    }

    /**
     * Checks if a user has one of the specified roles
     *
     * @param username the username of the user
     * @param roles the roles to check against
     * @return true if the user has one of the specified roles, false otherwise
     */
    public boolean hasRole(final String username, final String... roles) {
        String current = getRole(username);
        for (String role : roles) {
            if (role.equals(current)) {
                return true;
            }
        }
        return false;
    }
}
