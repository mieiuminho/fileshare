package server;

import exceptions.AuthenticationException;
import model.FileShare;
import util.BoundedBuffer;
import view.Terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

@SuppressWarnings({"checkstyle:VisibilityModifier", "checkstyle:MagicNumber"})
public final class Session implements Runnable {
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
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            synchronized (this.replies) {
                this.replies.put(this.id, new PrintWriter(this.socket.getOutputStream()));
            }

            String message;
            while ((message = in.readLine()) != null && !message.equals("quit")) {
                try {
                    this.requests.add(this.id + " " + message);
                } catch (InterruptedException e) {
                    PrintWriter out = this.replies.get(this.id);
                    synchronized (out) {
                        out.println("ERROR: Couldn't add your request (" + message + ")");
                        out.flush();
                    }
                }
            }

            synchronized (this.replies) {
                this.replies.remove(this.id);
            }

            this.socket.shutdownOutput();
            this.socket.shutdownInput();
            this.socket.close();
            Terminal.info("Session " + this.id + " finished!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void login(final String username, final String password) {
        try {
            this.model.login(username, password);
            this.loggedIn = username;
        } catch (AuthenticationException exception) {
            Terminal.error(exception.toString());
        }
    }

    private void logout() {
        this.loggedIn = null;
    }
}
