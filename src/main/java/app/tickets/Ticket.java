package app.tickets;

import app.tickets.enums.BusinessPriority;
import app.tickets.enums.ExpertiseArea;
import app.tickets.enums.Status;

public abstract class Ticket {
    private int id;
    private String type;
    private String title;
    private BusinessPriority businessPriority;
    private Status status;
    private ExpertiseArea expertiseArea;
    private String description; // optional
    private String reportedBy; // optional for some

    public Ticket(int id, String type, String title, BusinessPriority businessPriority, Status status, ExpertiseArea expertiseArea, String description, String reportedBy) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.businessPriority = businessPriority;
        this.status = Status.OPEN;
        this.expertiseArea = expertiseArea;
        this.description = description;
        this.reportedBy = reportedBy;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public BusinessPriority getBusinessPriority() {
        return businessPriority;
    }

    public Status getStatus() {
        return status;
    }

    public ExpertiseArea getExpertiseArea() {
        return expertiseArea;
    }

    public String getDescription() {
        return description;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBusinessPriority(BusinessPriority businessPriority) {
        this.businessPriority = businessPriority;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setExpertiseArea(ExpertiseArea expertiseArea) {
        this.expertiseArea = expertiseArea;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }
}