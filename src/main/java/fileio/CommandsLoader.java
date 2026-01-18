package fileio;

import app.utils.ResultUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class CommandsLoader {

    private CommandsLoader() {

    }

    /**
     * Loads the commands from a JSON file
     *
     * @param path the path to the JSON file
     * @return a list of CommandInput objects
     * @throws IOException if an I/O error occurs
     */
    public static List<CommandInput> load(final String path) throws IOException {
        ObjectMapper mapper = ResultUtils.MAPPER;
        CommandInput[] commands = mapper.readValue(
                new File(path),
                CommandInput[].class
        );
        return Arrays.asList(commands);
    }
}
