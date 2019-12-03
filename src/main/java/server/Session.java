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
import java.util.Map;

@SuppressWarnings({"checkstyle:VisibilityModifier", "checkstyle:MagicNumber"})
public final class Session implements Runnable {
    private static Logger log = LogManager.getLogger(Session.class);

    private int id;
    private final Socket socket;
    private BoundedBuffer<String> requests;
    private Map<Integer, PrintWriter> replies;
    private final FileShare model;
    private String loggedIn;

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

            synchronized (this.replies) {
                this.replies.put(this.id, new PrintWriter(this.socket.getOutputStream()));
            }

            String message;
            while ((message = in.readLine()) != null && !message.equals("quit")) {

                String[] split = message.split(" ");

                if (split[0].toLowerCase().equals("login")) {

                    PrintWriter p = this.replies.get(this.id);
                    log.trace("(" + this.id + ") request: login");

                    if (split.length != 3) {
                        log.error(this.id + ": Not enough arguments");
                        p.println("Error: Not enough arguments");
                        p.flush();
                        continue;
                    }

                    try {
                        this.login(split[0], split[1]);
                        p.println("Logged in as " + split[1]);
                        log.info(split[1] + " logged in");
                    } catch (Exception e) {
                        p.println("Error: " + e.getMessage());
                        log.error(this.id + ": " + e.getMessage());
                    } finally {
                        p.flush();
                        continue;
                    }
                }

                if (split[0].toLowerCase().equals("logout")) {

                    PrintWriter p = this.replies.get(this.id);
                    log.trace("(" + this.id + ") request: logout");

                    if (this.loggedIn != null) {
                        log.info("Logged Out");
                        p.println("Logged out");
                    } else {
                        log.error(this.id + ": Not logged in");
                        p.println("Error: Not logged in");
                    }

                    p.flush();
                    this.logout();
                    continue;

                }

                try {
                    log.trace("(" + this.id + ") request: " + message);
                    this.requests.add(this.id + " " + message);
                } catch (InterruptedException e) {
                    PrintWriter out = this.replies.get(this.id);
                    synchronized (out) {
                        out.println("ERROR: Couldn't add your request (" + message + ")");
                        out.flush();
                    }
                    log.error(this.id + ": " + e.getMessage());
                    e.printStackTrace();
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
