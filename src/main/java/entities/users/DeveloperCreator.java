package entities.users;

import fileio.UserInput;

import java.time.LocalDate;

public class DeveloperCreator implements UserCreator {
    /**
     * Creates a Developer user based on the provided input data
     *
     * @param userInput the input data for the user
     * @return the created Developer user
     */
    @Override
    public User create(final UserInput userInput) {
        return new Developer(userInput.getUsername(), userInput.getEmail(),
                LocalDate.parse(userInput.getHireDate()), userInput.getSeniority(),
                userInput.getExpertiseArea());
    }
}
