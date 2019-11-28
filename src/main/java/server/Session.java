package server;

import exceptions.AuthenticationException;
import model.FileShare;
import view.Terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@SuppressWarnings({"checkstyle:VisibilityModifier", "checkstyle:MagicNumber"})
public final class Session implements Runnable {
    private int id;
    private final Socket socket;
    private final FileShare model;
    private String loggedIn;

    public Session(final Socket socket, final int id, final FileShare model) {
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
                String response;
                try {
                    response = Worker.run(message.split(" "));
                } catch (Exception e) {
                    response = "ERROR: " + e.getMessage();
                }

                out.println(response);
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
