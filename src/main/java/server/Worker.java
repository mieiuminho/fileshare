package server;

import model.FileShare;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.BoundedBuffer;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;

import static java.util.Map.entry;

public final class Worker implements Runnable {
    private BoundedBuffer<String> requests;
    private Map<Integer, PrintWriter> replies;
    private FileShare model;

    private static Logger log = LogManager.getLogger(Worker.class);
    private static Map<String, Command> commands = Map.ofEntries(//
            entry("register", (argv, out, model) -> {
                String response = "REGISTER?";
                for (String arg : argv)
                    response += " " + arg;
                out.println(response);
                out.flush();
            }), entry("upload", (argv, out, model) -> {
                String response = "UPLOAD?";
                for (String arg : argv)
                    response += " " + arg;
                out.println(response);
                out.flush();
            }), entry("download", (argv, out, model) -> {
                String response = "DOWNLOAD?";
                for (String arg : argv)
                    response += " " + arg;
                out.println(response);
                out.flush();
            }), entry("search", (argv, out, model) -> {
                String response = "SEARCH?";
                for (String arg : argv)
                    response += " " + arg;
                out.println(response);
                out.flush();
            }) //
    );

    public Worker(final BoundedBuffer<String> requests, final Map<Integer, PrintWriter> replies,
            final FileShare model) {
        this.requests = requests;
        this.replies = replies;
        this.model = model;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String[] argv = this.requests.get().split("\\s+");
                int id = Integer.parseInt(argv[0]);
                @SuppressWarnings("checkstyle:AvoidInlineConditionals")
                String command = argv.length >= 2 ? argv[1].toLowerCase() : "HELP";
                PrintWriter out = this.replies.get(id);

                synchronized (out) {
                    if (Worker.commands.containsKey(command)) {
                        log.debug("(" + id + ") task: " + command);
                        Worker.commands.get(command).run(Arrays.copyOfRange(argv, 2, argv.length), out, this.model);
                    } else {
                        if (!command.equals("help")) {
                            log.warn("(" + id + ") request not available: " + command);
                        } else
                            log.debug("(" + id + ") request for help");
                        String listOfCommands = "List of Available Commands:";

                        for (String cmd : Worker.commands.keySet()) {
                            listOfCommands += " " + cmd + ";";
                        }

                        out.println(listOfCommands);
                        out.flush();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
