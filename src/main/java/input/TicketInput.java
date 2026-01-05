package input;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
//@JsonIgnoreProperties(ignoreUnknown = true)
public final class TicketInput {
    private String type;
    private String title;
    private String businessPriority;
    private String expertiseArea;
    private String description;
    private String reportedBy;

    // BUG specific fields
    private String expectedBehavior;
    private String actualBehavior;
    private String frequency;
    private String severity;
    private String environment;
    private Integer errorCode;

    // FEATURE_REQUEST specific fields
    private String businessValue;
    private String customerDemand;

    // UI_FEEDBACK specific fields
    private String uiElementId;
    private Integer usabilityScore;
    private String screenshotUrl;
    private String suggestedFix;
}