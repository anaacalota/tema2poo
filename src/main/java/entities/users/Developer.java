package entities.users;

import app.AppContext;
import entities.tickets.enums.ExpertiseArea;
import entities.tickets.enums.Priority;
import entities.users.enums.Seniority;
import entities.users.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

public class Developer extends User {
    @Getter
    private final LocalDate hireDate;
    @Getter
    private final Seniority seniority;
    @Getter
    private final ExpertiseArea expertiseArea;
    @Getter @Setter
    private double performanceScore = 0.0;

    public Developer(final String username, final String email,
                     final LocalDate hireDate, final Seniority seniority,
                     final ExpertiseArea expertiseArea) {
        super(username, email, UserRole.DEVELOPER);
        this.hireDate = hireDate;
        this.seniority = seniority;
        this.expertiseArea = expertiseArea;
    }

    /**
     * Determines if the developer can handle a ticket based on its type and priority
     *
     * @param type the type of the ticket
     * @param priority the priority of the ticket
     * @return true if the developer can handle the ticket, false otherwise
     */
    public boolean canHandleTicket(final String type, final Priority priority) {
        return switch (this.seniority) {
            case JUNIOR -> (priority == Priority.LOW || priority == Priority.MEDIUM)
                    && (type.equals("BUG") || type.equals("UI_FEEDBACK"));
            case MID -> (priority == Priority.LOW || priority == Priority.MEDIUM
                    || priority == Priority.HIGH)
                    && (type.equals("BUG") || type.equals("UI_FEEDBACK")
                    || type.equals("FEATURE_REQUEST"));
            case SENIOR -> true;
            default -> false;
        };
    }

    /**
     * Determines if the developer can handle a ticket based on its required expertise area
     *
     * @param requiredArea the required expertise area of the ticket
     * @return true if the developer can handle the ticket, false otherwise
     */
    public boolean canHandleExpertiseArea(final ExpertiseArea requiredArea) {
        return switch (this.expertiseArea) {
            case FRONTEND -> requiredArea == ExpertiseArea.FRONTEND
                    || requiredArea == ExpertiseArea.DESIGN;
            case BACKEND -> requiredArea == ExpertiseArea.BACKEND
                    || requiredArea == ExpertiseArea.DB;
            case FULLSTACK -> true;
            case DEVOPS -> requiredArea == ExpertiseArea.DEVOPS;
            case DESIGN -> requiredArea == ExpertiseArea.DESIGN
                    || requiredArea == ExpertiseArea.FRONTEND;
            case DB -> requiredArea == ExpertiseArea.DB;
            default -> false;
        };
    }

    /**
     * Determines if the user is a developer
     *
     * @return true because this is a developer
     */
    @Override
    public boolean isDeveloper() {
        return true;
    }

    /**
     * Determines if the user is a manager
     *
     * @return false because this is not a manager
     */
    @Override
    public boolean isManager() {
        return false;
    }

    /**
     * Updates the developer with a new notification message
     *
     * @param message the notification message
     */
    @Override
    public void update(final String message) {
        AppContext.getInstance().addNotification(getUsername(), message);
    }

}
