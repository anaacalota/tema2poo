package entities.tickets;

import entities.tickets.enums.BusinessValue;
import entities.tickets.enums.CustomerDemand;
import entities.tickets.enums.ExpertiseArea;
import entities.tickets.enums.Priority;
import lombok.Getter;

public class FeatureRequestTicket extends Ticket {
    @Getter
    private final BusinessValue businessValue;
    @Getter
    private final CustomerDemand customerDemand;

    public FeatureRequestTicket(final int id, final String title,
                                final Priority businessPriority,
                                final ExpertiseArea expertiseArea,
                                final String description, final String reportedBy,
                                final BusinessValue businessValue,
                                final CustomerDemand customerDemand) {
        super(id, "FEATURE_REQUEST", title, businessPriority, expertiseArea,
                description, reportedBy);
        this.businessValue = businessValue;
        this.customerDemand = customerDemand;
    }
}
