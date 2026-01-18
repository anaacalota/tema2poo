package entities.users;

import fileio.UserInput;

public final class UserFactory {
    private UserFactory() {

    }

    /**
     * Create user based on role
     *
     * @param userInput the input data for the user
     * @return the created User object
     */
    public static User createUser(final UserInput userInput) {
        String role = userInput.getRole();

        return switch (role) {
            case "REPORTER" -> new ReporterCreator().create(userInput);
            case "DEVELOPER" -> new DeveloperCreator().create(userInput);
            case "MANAGER" -> new ManagerCreator().create(userInput);
            default -> null;
        };
    }
}
