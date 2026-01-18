package commands;

import app.services.SearchService;
import app.services.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SearchCommand extends BaseCommand {

    private final SearchService searchService;
    private final JsonNode filters;

    public SearchCommand(UserService userService,
                         SearchService searchService,
                         String username,
                         String timestamp,
                         JsonNode filters) {
        super(userService, username, timestamp);
        this.searchService = searchService;
        this.filters = filters;
    }

    @Override
    public ObjectNode execute() {

        if (!userExists()) {
            return error("search",
                    "The user " + username + " does not exist.");
        }

        if (!hasRole("DEVELOPER", "MANAGER")) {
            return error("search",
                    "The user does not have permission to execute this command: " +
                            "required role MANAGER, DEVELOPER; user role " + userService.getRole(username) + ".");
        }

        return searchService.search(username, timestamp, filters);
    }
}
