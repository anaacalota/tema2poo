package entities.users;

import fileio.UserInput;

public interface UserCreator {
    /**
     * Creates a User object based on the provided UserInput
     *
     * @param userInput the input data for the user
     * @return the created User object
     */
    User create(UserInput userInput);
}
