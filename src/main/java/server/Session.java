package server;

import exceptions.AuthenticationException;
import model.FSModel;
import view.Terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@SuppressWarnings({"checkstyle:VisibilityModifier", "checkstyle:MagicNumber"})
public final class Session implements Runnable {

    public static String hostname = "127.0.0.1";
    public static int port = 12345;

    private int id;

    private final Socket socket;
    private final FSModel model;
    private String loggedIn;

    public Session(final Socket socket, final int id, final FSModel model) {
        this.socket = socket;
        this.id = id;
        this.model = model;
        this.loggedIn = null;
    }

    @SuppressWarnings("checkstyle:InnerAssignment")
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            PrintWriter out = new PrintWriter(this.socket.getOutputStream());
            String message;

            while ((message = in.readLine()) != null && !message.equals("quit")) {
                String[] argv = message.split(" ");
                Command cmd = Command.getValue(argv[0]);
                out.println("Reply from server: " + message + "(" + cmd + ")");
                out.flush();
            }

            this.socket.shutdownOutput();
            this.socket.shutdownInput();
            this.socket.close();
            Terminal.info("Session " + this.id + " finished!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void login(final String username, final String password) throws AuthenticationException {
        if (!this.model.containsUser(username)) {
            throw new AuthenticationException("Username isn't registered");
        }
        if (!this.model.matchPassword(username, password)) {
            throw new AuthenticationException("Incorrect password");
        }
        this.loggedIn = username;
    }
}
