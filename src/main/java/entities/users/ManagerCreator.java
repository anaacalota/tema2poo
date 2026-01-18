package entities.users;

import fileio.UserInput;

import java.time.LocalDate;

public class ManagerCreator implements UserCreator {
    /** Creates a Manager user based on the provided UserInput data
     *
     * @param userInput the input data for creating the Manager user
     * @return the created Manager user
     */
    @Override
    public User create(final UserInput userInput) {
        return new Manager(userInput.getUsername(), userInput.getEmail(),
                LocalDate.parse(userInput.getHireDate()),
                userInput.getSubordinates());
    }
}
