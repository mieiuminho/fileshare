package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import exceptions.AuthenticationException;
import exceptions.DuplicateUserException;

import model.FileShare;
import util.BoundedBuffer;
import util.Filter;

@SuppressWarnings({"checkstyle:VisibilityModifier", "checkstyle:MagicNumber"})
public final class Session implements Runnable {
    private int id;
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private BoundedBuffer<String> requests;
    private Map<Integer, PrintWriter> replies;
    private FileShare model;
    private String loggedIn;

    private static Logger log = LogManager.getLogger(Session.class);

    public Session(final int id, final Socket socket, final BoundedBuffer<String> requests,
            final Map<Integer, PrintWriter> replies, final FileShare model) {
        this.id = id;
        this.socket = socket;
        this.requests = requests;
        this.replies = replies;
        this.model = model;
        this.loggedIn = null;
    }

    private void login(final String[] argv) {
        String response = null;
        try {
            this.model.login(argv[0], argv[1]);
            this.loggedIn = argv[0];
            log.info("User " + argv[0] + " logged in");
            response = "REPLY: Logged in as " + argv[0];
        } catch (AuthenticationException e) {
            log.error(this.id + ": " + e.getMessage());
            response = "ERROR: " + e.getMessage();
        } catch (IndexOutOfBoundsException e) {
            log.error(this.id + ": wrong number of arguments");
            response = "ERROR: wrong number of arguments";
        } finally {
            synchronized (out) {
                this.out.println(response);
            }
        }
    }

    private void logout(final String[] argv) {
        String response;
        if (this.loggedIn != null) {
            this.loggedIn = null;
            log.info("Logged Out");
            response = "REPLY: Logged out";
        } else {
            log.error(this.id + ": Not logged in");
            response = "ERROR: Not logged in";
        }
        synchronized (out) {
            this.out.println(response);
        }
    }

    private void register(final String[] argv) {
        String response = null;
        try {
            this.model.registerUser(argv[0], argv[1]);
            log.info(argv[0] + " registered as a user");
            response = "REPLY: User registered";
        } catch (DuplicateUserException e) {
            log.error(this.id + ": " + e.getMessage());
            response = "ERROR: " + e.getMessage();
        } catch (ArrayIndexOutOfBoundsException e) {
            log.error(this.id + ": wrong number of arguments");
            response = "ERROR: wrong number of arguments";
        } finally {
            synchronized (this.out) {
                this.out.println(response);
            }
        }
    }

    private final Map<String, Filter> filters = Map.ofEntries(//
            Map.entry("login", this::login), //
            Map.entry("logout", this::logout), //
            Map.entry("register", this::register) //
    );

    @SuppressWarnings("checkstyle:InnerAssignment")
    @Override
    public void run() {
        log.info("Session " + this.id + " established on " + this.socket.getRemoteSocketAddress());

        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintWriter(this.socket.getOutputStream(), true);

            synchronized (this.replies) {
                this.replies.put(this.id, this.out);
            }

            String message;
            while ((message = in.readLine()) != null) {
                String[] argv = message.trim().split("\\s+");
                String command = argv[0].toLowerCase();

                if (this.filters.containsKey(command)) {
                    log.trace("(" + this.id + ") request: " + message);
                    this.filters.get(command).execute(Arrays.copyOfRange(argv, 1, argv.length));
                } else if (Worker.OPTIONS.contains(command)) {
                    try {
                        if (!command.equals("data"))
                            log.trace("(" + this.id + ") request: " + message);
                        if (this.loggedIn != null) {
                            this.requests.add(this.id + " " + message);
                        } else {
                            synchronized (this.out) {
                                log.info("Request while not logged in");
                                this.out.println("ERROR: You need to be logged in");
                            }
                        }
                    } catch (InterruptedException e) {
                        synchronized (this.out) {
                            this.out.println("ERROR: Couldn't add your request (" + message + ")");
                        }
                        log.error(this.id + ": " + e.getMessage());
                    }
                } else {
                    log.error("Not a valid command: " + message);
                    synchronized (this.out) {
                        this.out.println("ERROR: Not a valid command. Use help to learn more.");
                    }
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
        }
    }
}
