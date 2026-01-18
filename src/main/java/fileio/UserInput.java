package fileio;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import entities.tickets.enums.ExpertiseArea;
import entities.users.enums.Seniority;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class UserInput {
    private String username;
    private String role;
    private String email;
    private String hireDate;
    private Seniority seniority;
    private ExpertiseArea expertiseArea;
    private List<String> subordinates;
}
