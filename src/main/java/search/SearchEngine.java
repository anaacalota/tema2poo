package search;

import java.util.List;

public class SearchEngine<T> {
    /**
     * Filters a list of items by applying a set of search strategies
     *
     * @param items the collection of items to be filtered
     * @param strategies the list of search criteria to apply
     * @return a list containing only the items that match every strategy
     */
    public List<T> apply(final List<T> items, final List<SearchStrategy<T>> strategies) {
        return items.stream()
                .filter(item -> strategies.stream().allMatch(strategy -> strategy.matches(item)))
                .toList();
    }
}
