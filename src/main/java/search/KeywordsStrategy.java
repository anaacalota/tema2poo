package search;

import entities.tickets.Ticket;

import java.util.ArrayList;
import java.util.List;

public class KeywordsStrategy implements SearchStrategy<Ticket> {
    private final List<String> words;

    /**
     * Initializes the strategy with a list of keywords for case-insensitive matching
     *
     * @param words the list of search terms to check for
     */
    public KeywordsStrategy(final List<String> words) {
        List<String> lowerCaseWords = new ArrayList<>();
        for (final String word : words) {
            lowerCaseWords.add(word.toLowerCase());
        }
        this.words = lowerCaseWords;
    }

    /**
     * Checks if any of the keywords are contained in the ticket title or description
     *
     * @param ticket the ticket to evaluate
     * @return true if at least one keyword is found in the ticket's text
     */
    @Override
    public boolean matches(final Ticket ticket) {
        String text = (ticket.getTitle() + " " + ticket.getDescription()).toLowerCase();

        for (final String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }

        return false;
    }
}
