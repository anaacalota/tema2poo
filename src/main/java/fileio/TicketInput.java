package fileio;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import entities.tickets.enums.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class TicketInput {
    private int id;
    private String type;
    private String title;
    private Priority businessPriority;
    private Status status;
    private ExpertiseArea expertiseArea;
    private String description;
    private String reportedBy;
    private String assignedTo;

    // BUG-specific fields
    private String expectedBehavior;
    private String actualBehavior;
    private Frequency frequency;
    private Severity severity;
    private String environment;
    private Integer errorCode;

    // FEATURE_REQUEST specific fields
    private BusinessValue businessValue;
    private CustomerDemand customerDemand;

    // UI_FEEDBACK specific fields
    private String uiElementId;
    private Integer usabilityScore;
    private String screenshotUrl;
    private String suggestedFix;
}
