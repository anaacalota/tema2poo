package entities.users;

import app.AppContext;
import entities.users.enums.UserRole;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Manager extends User {
    @Getter
    private final LocalDate hireDate;
    @Getter
    private final List<String> subordinates;

    public Manager(final String username, final String email,
                   final LocalDate hireDate, final List<String> subordinates) {
        super(username, email, UserRole.MANAGER);
        this.hireDate = hireDate;
        if (subordinates != null) {
            this.subordinates = new ArrayList<>(subordinates);
        } else {
            this.subordinates = new ArrayList<>();
        }
    }

    /**
     * Checks if the user is a manager
     *
     * @return true because this user is a manager
     */
    @Override
    public boolean isManager() {
        return true;
    }

    /**
     * Checks if the user is a developer
     *
     * @return false because this user is not a developer
     */
    @Override
    public boolean isDeveloper() {
        return false;
    }

    /**
     * Updates the manager with a new notification message
     *
     * @param message the notification message
     */
    @Override public void update(final String message) {
        AppContext.getInstance().addNotification(getUsername(), message);
    }
}
