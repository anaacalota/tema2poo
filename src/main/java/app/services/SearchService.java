package app.services;

import app.AppContext;
import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.tickets.Ticket;
import entities.tickets.enums.Priority;
import entities.users.Developer;
import entities.users.Manager;
import entities.users.User;
import entities.users.enums.Seniority;
import entities.users.enums.UserRole;
import search.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SearchService {
    private final AppContext context;
    private final TicketService ticketService;

    public SearchService(final TicketService ticketService) {
        this.context = AppContext.getInstance();
        this.ticketService = ticketService;
    }

    /**
     * Executes a search command based on the user's role and provided filters
     *
     * @param username the name of the user performing the search
     * @param timestamp the time of the command execution
     * @param filters the JSON node containing filtering criteria
     * @return an ObjectNode containing search results or an error message
     */
    public ObjectNode search(final String username, final String timestamp,
                             final JsonNode filters) {
        User user = context.getUser(username);
        ObjectNode result = ResultUtils.createBaseNode("search", username, timestamp);

        if (user == null) {
            result.put("error", "The user " + username + " does not exist.");
            return result;
        }

        String searchType = filters.get("searchType").asText();
        result.put("searchType", searchType);

        if (user.getRole() == UserRole.DEVELOPER) {
            return searchTickets(user, filters, result);
        }

        if (user.getRole() == UserRole.MANAGER) {
            if (searchType.equals("TICKET")) {
                return searchTickets(user, filters, result);
            } else {
                return searchDevelopersAsManager((Manager) user, filters, result);
            }
        }

        return result;
    }

    /**
     * Filters and retrieves tickets based on the user's permissions and input filters
     *
     * @param user the user performing the ticket search
     * @param filters the JSON node containing ticket filters
     * @param result the base output node to populate
     * @return the populated result node with filtered tickets
     */
    private ObjectNode searchTickets(final User user, final JsonNode filters,
                                     final ObjectNode result) {
        List<Ticket> candidates;
        List<SearchStrategy<Ticket>> strategies;
        boolean includeMatchingWords = false;

        if (user.isDeveloper()) {
            candidates = ticketService.ticketsForDeveloper(user.getUsername());
            strategies = buildTicketStrategies(filters, (Developer) user);
        } else if (user.isManager()) {
            candidates = ticketService.ticketsForManager();
            strategies = buildTicketStrategies(filters, null);
            includeMatchingWords = true;
        } else {
            return result;
        }

        SearchEngine<Ticket> engine = new SearchEngine<>();
        List<Ticket> filtered = new ArrayList<>(engine.apply(candidates, strategies));
        filtered.sort(Comparator.comparing(Ticket::getCreatedAt).thenComparing(Ticket::getId));

        ArrayNode array = ResultUtils.MAPPER.createArrayNode();
        for (Ticket ticket : filtered) {
            ObjectNode node = ticketService.ticketToJsonSearch(ticket);

            if (includeMatchingWords) {
                if (filters.has("keywords")) {
                    node.set("matchingWords", ResultUtils.MAPPER.valueToTree(
                            extractMatchingWords(ticket, filters.get("keywords"))));
                } else {
                    node.set("matchingWords", ResultUtils.MAPPER.createArrayNode());
                }
            }
            array.add(node);
        }

        result.set("results", array);
        return result;
    }

    /**
     * Searches for developers within a manager's team using specified filters
     *
     * @param manager the manager searching for subordinates
     * @param filters the JSON node containing developer filters
     * @param result the base output node to populate
     * @return the populated result node with filtered developers
     */
    private ObjectNode searchDevelopersAsManager(final Manager manager, final JsonNode filters,
                                                 final ObjectNode result) {
        List<String> subordinates = manager.getSubordinates();
        List<Developer> candidates = context.getAllUsers().stream()
                .filter(User::isDeveloper).map(user -> (Developer) user)
                .filter(dev -> subordinates.contains(dev.getUsername()))
                .collect(Collectors.toList());

        List<SearchStrategy<Developer>> strategies = buildDeveloperStrategies(filters);
        SearchEngine<Developer> engine = new SearchEngine<>();
        List<Developer> filtered = new ArrayList<>(engine.apply(candidates, strategies));

        filtered.sort(Comparator.comparing(Developer::getUsername));

        ArrayNode array = ResultUtils.MAPPER.createArrayNode();
        for (Developer dev : filtered) {
            ObjectNode node = ResultUtils.MAPPER.createObjectNode();
            node.put("username", dev.getUsername());
            node.put("expertiseArea", dev.getExpertiseArea().name());
            node.put("seniority", dev.getSeniority().name());
            node.put("performanceScore", dev.getPerformanceScore());
            node.put("hireDate", dev.getHireDate().toString());
            array.add(node);
        }

        result.set("results", array);
        return result;
    }

    /**
     * Creates a list of search strategies for tickets based on the input filters
     *
     * @param filters the JSON node containing ticket criteria
     * @param dev the developer
     * @return a list of active ticket search strategies
     */
    private List<SearchStrategy<Ticket>> buildTicketStrategies(final JsonNode filters,
                                                               final Developer dev) {
        List<SearchStrategy<Ticket>> strategies = new ArrayList<>();

        if (filters.has("businessPriority")) {
            strategies.add(new BusinessPriorityStrategy(Priority.valueOf(
                    filters.get("businessPriority").asText())));
        }
        if (filters.has("type")) {
            strategies.add(new TypeStrategy(filters.get("type").asText()));
        }
        if (filters.has("createdAfter")) {
            strategies.add(new CreatedAfterStrategy(LocalDate.parse(
                    filters.get("createdAfter").asText())));
        }
        if (filters.has("createdBefore")) {
            strategies.add(new CreatedBeforeStrategy(LocalDate.parse(
                    filters.get("createdBefore").asText())));
        }
        if (filters.has("availableForAssignment")
                && filters.get("availableForAssignment").asBoolean() && dev != null) {
            strategies.add(new AvailableForAssignmentStrategy(dev));
        }
        if (filters.has("keywords")) {
            List<String> words = new ArrayList<>();
            filters.get("keywords").forEach(node -> words.add(node.asText()));
            strategies.add(new KeywordsStrategy(words));
        }

        return strategies;
    }

    /**
     * Creates a list of search strategies for developers based on the input filters
     *
     * @param filters the JSON node containing developer criteria
     * @return a list of active developer search strategies
     */
    private List<SearchStrategy<Developer>> buildDeveloperStrategies(final JsonNode filters) {
        List<SearchStrategy<Developer>> strategies = new ArrayList<>();

        if (filters.has("expertiseArea")) {
            strategies.add(new ExpertiseStrategy(filters.get("expertiseArea").asText()));
        }
        if (filters.has("seniority")) {
            strategies.add(new SeniorityStrategy(Seniority.valueOf(
                    filters.get("seniority").asText())));
        }
        if (filters.has("performanceScoreAbove")) {
            strategies.add(new PerformanceScoreAboveStrategy(
                    filters.get("performanceScoreAbove").asDouble()));
        }
        if (filters.has("performanceScoreBelow")) {
            strategies.add(new PerformanceScoreBelowStrategy(
                    filters.get("performanceScoreBelow").asDouble()));
        }
        return strategies;
    }

    /**
     * Finds and sorts keywords that appear in a ticket's title or description
     *
     * @param ticket the ticket to check against
     * @param keywordsNode the JSON array of keywords to look for
     * @return a sorted list of keywords that matched the ticket content
     */
    private List<String> extractMatchingWords(final Ticket ticket, final JsonNode keywordsNode) {
        String text = (ticket.getTitle() + " " + ticket.getDescription()).toLowerCase();
        List<String> matched = new ArrayList<>();

        for (JsonNode node : keywordsNode) {
            String word = node.asText().toLowerCase();
            if (text.contains(word)) {
                matched.add(word);
            }
        }
        matched.sort(String::compareTo);
        return matched;
    }
}
