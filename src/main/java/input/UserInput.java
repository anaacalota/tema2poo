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
public final class UserInput {
    private String username;
    private String role;
    private String email;
    private String hireDate;
    private String seniority;
    private String expertiseArea;
    private List<String> subordinates;
}