package server;

import view.Terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@SuppressWarnings({"checkstyle:VisibilityModifier", "checkstyle:MagicNumber"})
public final class Connection implements Runnable {
    public static String hostname = "127.0.0.1";
    public static int port = 12345;

    private int id;
    private final Socket socket;

    public Connection(final Socket socket, final int id) {
        this.socket = socket;
        this.id = id;
    }

    @SuppressWarnings("checkstyle:InnerAssignment")
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            PrintWriter out = new PrintWriter(this.socket.getOutputStream());
            String message;

            while ((message = in.readLine()) != null && !message.equals("quit")) {
                out.println("Reply from server: " + message);
                out.flush();
            }

            this.socket.shutdownOutput();
            this.socket.shutdownInput();
            this.socket.close();
            Terminal.info("Connection " + this.id + " finished!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
