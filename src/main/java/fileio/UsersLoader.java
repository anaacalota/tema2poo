package fileio;

import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class UsersLoader {

    private UsersLoader() {

    }

    /**
     * Loads the users from a JSON file
     *
     * @param path the path to the JSON file
     * @return a list of UserInput objects
     * @throws IOException if an I/O error occurs
     */
    public static List<UserInput> load(final String path) throws IOException {
        ObjectMapper mapper = ResultUtils.MAPPER;
        UserInput[] users = mapper.readValue(
                new File(path),
                UserInput[].class
        );
        return Arrays.asList(users);
    }
}
