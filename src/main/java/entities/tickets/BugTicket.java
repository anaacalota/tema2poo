package entities.tickets;

import entities.tickets.enums.ExpertiseArea;
import entities.tickets.enums.Frequency;
import entities.tickets.enums.Priority;
import entities.tickets.enums.Severity;
import entities.tickets.enums.Status;
import lombok.Getter;

@Getter
public class BugTicket extends Ticket {
    private final String expectedBehavior;
    private final String actualBehavior;
    private final Frequency frequency;
    private final Severity severity;
    private final String environment;
    private final Integer errorCode;

    public BugTicket(final int id, final String title,
                     final Priority businessPriority, final ExpertiseArea expertiseArea,
                     final String description, final String reportedBy,
                     final String expectedBehavior, final String actualBehavior,
                     final Frequency frequency, final Severity severity,
                     final String environment, final Integer errorCode) {
        super(id, "BUG", title, businessPriority, expertiseArea, description, reportedBy);
        this.expectedBehavior = expectedBehavior;
        this.actualBehavior = actualBehavior;
        this.frequency = frequency;
        this.severity = severity;
        this.environment = environment;
        this.errorCode = errorCode;
        this.setStatus(Status.OPEN);
    }
}
