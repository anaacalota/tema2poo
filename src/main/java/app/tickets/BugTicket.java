package app.tickets;

import app.tickets.enums.*;

public class BugTicket extends Ticket {
    private String expectedBehavior;
    private String actualBehavior;
    private Frequency frequency;
    private Severity severity;
    private String environment; // optional
    private Integer errorCode; // optional

    public BugTicket(int id, String type, String title, BusinessPriority businessPriority, Status status, ExpertiseArea expertiseArea, String description, String reportedBy, String expectedBehavior, String actualBehavior, Frequency frequency, Severity severity, String environment, Integer errorCode) {
        super(id, "Bug", title, businessPriority, status, expertiseArea, description, reportedBy);
        this.expectedBehavior = expectedBehavior;
        this.actualBehavior = actualBehavior;
        this.frequency = frequency;
        this.severity = severity;
        this.environment = environment;
        this.errorCode = errorCode;
    }
}
