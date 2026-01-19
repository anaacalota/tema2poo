package entities.tickets;

import entities.comments.Comment;
import entities.tickets.enums.ExpertiseArea;
import entities.tickets.enums.Priority;
import entities.tickets.enums.Status;
import entities.users.enums.Seniority;
import lombok.Getter;
import lombok.Setter;
import notifications.Subject;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public abstract class Ticket extends Subject {
    @Getter
    private final int id;
    @Getter
    private final String type;
    @Getter
    private final String title;
    @Getter
    private final ExpertiseArea expertiseArea;
    @Getter
    private final String description;
    @Setter
    @Getter
    private Priority businessPriority;
    @Getter
    private Status status;
    @Setter
    @Getter
    private String reportedBy;
    @Setter
    @Getter
    private LocalDate createdAt;
    @Setter
    @Getter
    private LocalDate solvedAt;
    @Setter
    @Getter
    private LocalDate assignedAt;
    @Setter
    @Getter
    private String assignedTo;
    @Getter
    private final List<Comment> comments = new ArrayList<>();
    private final List<Status> previousStatuses = new ArrayList<>();
    @Getter
    private final List<HistoryEntry> history = new ArrayList<>();

    protected Ticket(final int id, final String type,
                     final String title, final Priority businessPriority,
                     final ExpertiseArea expertiseArea, final String description,
                     final String reportedBy) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.businessPriority = businessPriority;
        this.expertiseArea = expertiseArea;
        this.description = description;
        this.reportedBy = reportedBy;
    }

    /**
     * Sets the status of the ticket and notifies observers if it changes
     *
     * @param newStatus the new status to set
     */
    public void setStatus(final Status newStatus) {
        if (this.status != newStatus) {
            Status oldStatus = this.status;
            this.status = newStatus;

            notifyObservers("Ticket " + id + " changed status from " + oldStatus
                    + " to " + newStatus + ".");
        }
    }

    /**
     * Pushes the current status onto the previous statuses stack
     *
     * @param prevStatus the status to push
     */
    public void pushPreviousStatus(final Status prevStatus) {
        previousStatuses.add(prevStatus);
    }

    /**
     * Pops the last status from the previous statuses stack
     *
     * @return the last previous status, or null if none exist
     */
    public Status popPreviousStatus() {
        if (previousStatuses.isEmpty()) {
            return null;
        }
        return previousStatuses.removeLast();
    }

    /**
     * Checks if there are any previous statuses stored
     *
     * @return true if there are previous statuses, false otherwise
     */
    public boolean hasPreviousStatus() {
        return !previousStatuses.isEmpty();
    }

    /**
     * Adds an entry to the ticket's history
     *
     * @param entry the history entry to add
     */
    public void addHistoryEntry(final HistoryEntry entry) {
        history.add(entry);
    }

    /**
     * Calculates the number of days taken to resolve the ticket
     *
     * @return the number of days from assignment to resolution, or 0 if not resolved
     */
    public int daysToResolve() {
        if (assignedAt == null || solvedAt == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(assignedAt, solvedAt) + 1;
    }

    /**
     * Gets the possible expertise areas for each case
     *
     * @return a list of expertise areas
     */
    public List<ExpertiseArea> getRequiredExpertiseAreasSorted() {
        List<ExpertiseArea> list = new ArrayList<>();

        switch (this.expertiseArea) {
            case FRONTEND:
                list.add(ExpertiseArea.FRONTEND);
                list.add(ExpertiseArea.DESIGN);
                list.add(ExpertiseArea.FULLSTACK);
                break;
            case BACKEND, DB:
                list.add(ExpertiseArea.BACKEND);
                list.add(ExpertiseArea.DB);
                list.add(ExpertiseArea.FULLSTACK);
                break;
            case DEVOPS:
                list.add(ExpertiseArea.DEVOPS);
                list.add(ExpertiseArea.FULLSTACK);
                break;
            case DESIGN:
                list.add(ExpertiseArea.DESIGN);
                list.add(ExpertiseArea.FRONTEND);
                list.add(ExpertiseArea.FULLSTACK);
                break;
            case FULLSTACK:
                list.add(ExpertiseArea.FULLSTACK);
                break;
            default:
                break;
        }

        return list;
    }

    /**
     * Gets the possible seniority levels for each case
     *
     * @return a list of seniority levels
     */
    public List<Seniority> getRequiredSenioritiesSorted() {
        List<Seniority> list = new ArrayList<>();

        switch (this.businessPriority) {
            case LOW:
            case MEDIUM:
                if (!this.type.equals("FEATURE_REQUEST")) {
                    list.add(Seniority.JUNIOR);
                }
                list.add(Seniority.MID);
                list.add(Seniority.SENIOR);
                break;
            case HIGH:
                list.add(Seniority.MID);
                list.add(Seniority.SENIOR);
                break;
            case CRITICAL:
                list.add(Seniority.SENIOR);
                break;
            default:
                break;
        }

        return list;
    }
}
