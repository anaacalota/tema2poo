package input;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
//@JsonIgnoreProperties(ignoreUnknown = true)
public final class FiltersInput {
    private String searchType;
    private String businessPriority;
    private String type;
    private String createdAt;
    private String createdBefore;
    private String createdAfter;
    private Boolean availableForAssignment;
    private List<String> keywords;
    private String expertiseArea;
    private String seniority;
    private Double performanceScoreAbove;
    private Double performanceScoreBelow;
}