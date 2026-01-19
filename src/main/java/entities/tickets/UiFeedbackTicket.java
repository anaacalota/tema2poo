package entities.tickets;

import entities.tickets.enums.Priority;
import entities.tickets.enums.BusinessValue;
import entities.tickets.enums.ExpertiseArea;
import lombok.Getter;

public class UiFeedbackTicket extends Ticket {
    private final String uiElementId;
    @Getter
    private final BusinessValue businessValue;
    @Getter
    private final int usabilityScore;
    private final String screenshotUrl;
    private final String suggestedFix;

    public UiFeedbackTicket(final int id, final String title,
                            final Priority businessPriority, final ExpertiseArea expertiseArea,
                            final String description, final String reportedBy,
                            final String uiElementId, final BusinessValue businessValue,
                            final int usabilityScore, final String screenshotUrl,
                            final String suggestedFix) {
        super(id, "UI_FEEDBACK", title, businessPriority, expertiseArea, description, reportedBy);
        this.uiElementId = uiElementId;
        this.businessValue = businessValue;
        this.usabilityScore = usabilityScore;
        this.screenshotUrl = screenshotUrl;
        this.suggestedFix = suggestedFix;
    }
}
