package search;

public interface SearchStrategy<T> {
    /**
     * Determines if the given item satisfies the specific search criteria
     *
     * @param item the object to be evaluated
     * @return true if the item matches the strategy, false otherwise
     */
    boolean matches(T item);
}
