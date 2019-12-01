package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import util.Parse;
import view.Terminal;


public final class Client {
    private static final String HOSTNAME = System.getenv("FILESHARE_SERVER_HOSTNAME");
    private static final int PORT = Integer.parseInt(System.getenv("FILESHARE_SERVER_PORT"));

    private Socket socket;

    public Client() {
    }

    public static void main(final String[] args) {
        Client.welcome();
        new Client().startUp();
    }

    @SuppressWarnings("checkstyle:InnerAssignment")
    public void startUp() {
        try {
            this.socket = new Socket(HOSTNAME, PORT);
            Terminal.info("Client is up at " + socket.getLocalSocketAddress());
            Terminal.info("Session established with server on " + socket.getRemoteSocketAddress());

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            String message;

            System.out.print("> ");
            while ((message = input.readLine()) != null && !message.equals("quit")) {
                out.println(message);
                out.flush();
                System.out.println(in.readLine());
                System.out.print("> ");
            }

            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void welcome() {
        Terminal.clear();
        List<String> logo = Parse.read(Client.class.getResource("../art/logo.ascii").toString().split(":")[1]);

        Terminal.show(logo);
    }
}
