package server;

import exceptions.AuthenticationException;
import model.FileShare;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.BoundedBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;

import static java.util.Map.entry;

@SuppressWarnings({"checkstyle:VisibilityModifier", "checkstyle:MagicNumber"})
public final class Session implements Runnable {
    private static Logger log = LogManager.getLogger(Session.class);

    private int id;
    private final Socket socket;
    private BoundedBuffer<String> requests;
    private Map<Integer, PrintWriter> replies;
    private final FileShare model;
    private String loggedIn;

    private Map<String, Filter> filters = Map.ofEntries(//
            entry("login", (argv, out) -> {
                String response = null;
                try {
                    this.login(argv[0], argv[1]);
                    log.info(argv[1] + " logged in");
                    response = "Logged in as " + argv[0];
                } catch (AuthenticationException e) {
                    log.error(this.id + ": " + e.getMessage());
                    response = "Error: " + e.getMessage();
                } catch (IndexOutOfBoundsException e) {
                    log.error(this.id + ": wrong number of arguments");
                    response = "Error: wrong number of arguments";
                } finally {
                    synchronized (out) {
                        out.println(response);
                        out.flush();
                    }
                }
            }), entry("logout", (argv, out) -> {
                String response;
                if (this.loggedIn != null) {
                    log.info("Logged Out");
                    response = "Logged out";
                } else {
                    log.error(this.id + ": Not logged in");
                    response = "Error: Not logged in";
                }
                synchronized (out) {
                    out.println(response);
                    out.flush();
                }
            }), entry("register", (argv, out) -> {
                String response = "REGISTER?";
                for (String arg : argv)
                    response += " " + arg;
                synchronized (out) {
                    out.println(response);
                    out.flush();
                }
            }), entry("help", (argv, out) -> {
                String response = "List of Available Commands:";

                for (String cmd : this.filters.keySet()) {
                    response += " " + cmd + ";";
                }

                for (String cmd : Worker.commands.keySet()) {
                    response += " " + cmd + ";";
                }

                synchronized (out) {
                    out.println(response);
                    out.flush();
                }
            }) //
    );

    public Session(final int id, final Socket socket, final BoundedBuffer<String> requests,
            final Map<Integer, PrintWriter> replies, final FileShare model) {
        this.id = id;
        this.socket = socket;
        this.requests = requests;
        this.replies = replies;
        this.model = model;
        this.loggedIn = null;
    }

    @SuppressWarnings("checkstyle:InnerAssignment")
    @Override
    public void run() {
        log.info("Session " + this.id + " established on " + this.socket.getRemoteSocketAddress());

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            PrintWriter out = new PrintWriter(this.socket.getOutputStream());

            synchronized (this.replies) {
                this.replies.put(this.id, out);
            }

            String message;
            while ((message = in.readLine()) != null && !message.equals("quit")) {
                String[] argv = message.trim().split("\\s+");
                String command = argv[0].toLowerCase();

                if (this.filters.containsKey(command)) {
                    log.trace("(" + this.id + ") request: " + message);
                    this.filters.get(command).execute(Arrays.copyOfRange(argv, 1, argv.length), out);
                } else if (Worker.commands.containsKey(command)) {
                    try {
                        log.trace("(" + this.id + ") request: " + message);
                        if (this.loggedIn != null) {
                            this.requests.add(this.id + " " + message);
                        } else {
                            synchronized (out) {
                                log.info("Request while not logged in");
                                out.println("ERROR: You need to be logged in");
                                out.flush();
                            }
                        }
                    } catch (InterruptedException e) {
                        synchronized (out) {
                            out.println("ERROR: Couldn't add your request (" + message + ")");
                            out.flush();
                        }
                        log.error(this.id + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    log.error("Not a valid command: " + message);
                    this.filters.get("help").execute(Arrays.copyOfRange(argv, 1, argv.length), out);
                }
            }

            synchronized (this.replies) {
                this.replies.remove(this.id);
            }

            this.socket.shutdownOutput();
            this.socket.shutdownInput();
            this.socket.close();
            log.info("Session " + this.id + " finished!");
        } catch (IOException e) {
            log.error(this.id + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void login(final String username, final String password) throws AuthenticationException {
        this.model.login(username, password);
        this.loggedIn = username;
    }

    private void logout() {
        this.loggedIn = null;
    }
}
