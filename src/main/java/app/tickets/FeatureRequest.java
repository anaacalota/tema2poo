package app.tickets;

import app.tickets.enums.*;

public class FeatureRequest extends Ticket {
    private BusinessValue businessValue;
    private CustomerDemand customerDemand;

    public FeatureRequest(int id, String type, String title, BusinessPriority businessPriority, Status status, ExpertiseArea expertiseArea, String description, String reportedBy, BusinessValue businessValue, CustomerDemand customerDemand) {
        super(id, "FeatureRequest", title, businessPriority, status, expertiseArea, description, reportedBy);
        this.businessValue = businessValue;
        this.customerDemand = customerDemand;
    }
}