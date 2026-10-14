package entities.users;

import entities.tickets.enums.Priority;
import entities.users.enums.UserRole;
import lombok.Getter;
import notifications.Observer;

public abstract class User implements Observer {
    @Getter
    private final String username;
    private final String email;
    @Getter
    private final UserRole role;

    protected User(final String username, final String email,
                   final UserRole role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }

    /**
     * Checks whether this user can handle a ticket. Non-developers do not
     * restrict ticket assignments.
     *
     * @param type the ticket type
     * @param priority the ticket priority
     * @return true for non-developers; developers provide their own check
     */
    public boolean canHandleTicket(final String type, final Priority priority) {
        return true;
    }

    /**
     * Determines if the user is a developer
     *
     * @return true if the user is a developer, false otherwise
     */
    public abstract boolean isDeveloper();

    /**
     * Determines if the user is a reporter
     *
     * @return true if the user is a reporter, false otherwise
     */
    public abstract boolean isManager();
}
