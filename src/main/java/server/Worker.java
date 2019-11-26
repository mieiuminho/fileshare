package server;

import java.util.Arrays;
import java.util.Map;

import static java.util.Map.entry;

public final class Worker {
    private Worker() {
    }

    private static Map<String, Command> commands = Map.ofEntries(entry("register", new Command() {
        @Override
        public String run(final String[] argv) {
            String response = "REGISTER?";
            for (String arg : argv)
                response += " " + arg;
            return response;
        }
    }), entry("login", new Command() {
        @Override
        public String run(final String[] argv) {
            String response = "LOGIN?";
            for (String arg : argv)
                response += " " + arg;
            return response;
        }
    }), entry("upload", new Command() {
        @Override
        public String run(final String[] argv) {
            String response = "UPLOAD?";
            for (String arg : argv)
                response += " " + arg;
            return response;
        }
    }), entry("download", new Command() {
        @Override
        public String run(final String[] argv) {
            String response = "DOWNLOAD?";
            for (String arg : argv)
                response += " " + arg;
            return response;
        }
    }), entry("search", new Command() {
        @Override
        public String run(final String[] argv) {
            String response = "SEARCH?";
            for (String arg : argv)
                response += " " + arg;
            return response;
        }
    }));

    public static String run(final String[] argv) throws Exception {
        String command = argv[0].toLowerCase();

        if (command.equals("help")) {
            String listOfCommands = "List of Available Commands:";

            for (String cmd : Worker.commands.keySet()) {
                listOfCommands += " " + cmd + ";";
            }

            return listOfCommands;
        }

        if (commands.containsKey(command)) {
            return commands.get(command).run(Arrays.copyOfRange(argv, 1, argv.length));
        } else
            throw new Exception("Operation not supported");
    }
}
