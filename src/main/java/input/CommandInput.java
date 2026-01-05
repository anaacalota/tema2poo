package input;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.List;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
//@JsonIgnoreProperties(ignoreUnknown = true)
public final class CommandInput {
    private String command;
    private String username;
    private String timestamp;
    private JsonNode params;
    private Integer ticketID;
    private String comment;
    private String name;
    private String dueDate;
    private List<String> blockingFor;
    private List<Integer> tickets;
    private List<String> assignedDevs;
    private Map<String, Object> filters;
}