package notifications;

import java.util.ArrayList;
import java.util.List;

public abstract class Subject {

    private final List<Observer> observers = new ArrayList<>();

    /**
     * Adds an observer to the list of observers
     *
     * @param observer the observer to be added
     */
    public void addObserver(final Observer observer) {
        if (observers.contains(observer)) {
            return;
        }
        observers.add(observer);
    }

    /**
     * Removes an observer from the list of observers
     *
     * @param observer the observer to be removed
     */
    public void removeObserver(final Observer observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all observers with a message
     *
     * @param message the message to be sent to observers
     */
    protected void notifyObservers(final String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }
}
