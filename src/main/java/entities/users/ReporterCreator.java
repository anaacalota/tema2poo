package entities.users;

import fileio.UserInput;

public class ReporterCreator implements UserCreator {
    /**
     * Creates a Reporter object based on the provided UserInput
     *
     * @param userInput the input data for the user
     * @return the created Reporter object
     */
    @Override
    public User create(final UserInput userInput) {
        return new Reporter(userInput.getUsername(), userInput.getEmail());
    }
}
