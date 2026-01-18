package notifications;

public interface Observer {
    /**
     * Method to update the observer with a new message
     *
     * @param message the message to be sent to the observer
     */
    void update(String message);
}
