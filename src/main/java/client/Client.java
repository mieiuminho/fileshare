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
    private static final int KB = 1024;
    private static final int MAXSIZE = Integer.parseInt(System.getenv("FILESHARE_MAX_MEMORY_SIZE")) * KB;

    private Socket socket;
    private ReplyHandler replyHandler;
    private BufferedReader in;
    private PrintWriter out;

    public Client() {
    }

    public static void main(final String[] args) {
        Client.welcome();
        new Client().startUp();
    }

    public static int getMAXSIZE() {
        return Client.MAXSIZE;
    }

    public void startUp() {
        try {
            this.connect();
            this.parse();
            this.disconnect();
        } catch (IOException e) {
            Terminal.error(e.getMessage());
            System.exit(1);
        }
    }

    public void parse() throws IOException {
        String message;
        while ((message = Terminal.input()) != null) {
            @SuppressWarnings("checkstyle:AvoidInlineConditionals")
            String command = message.contains(" ") ? message.split(" ")[0] : message;

            switch (command) {
                case "help":
                    Terminal.message("help");
                    break;
                case "exit":
                case "quit":
                    this.disconnect();
                    System.exit(0);
                    break;
                default:
                    out.println(message);
            }
        }
    }

    public void connect() throws IOException {
        this.socket = new Socket(HOSTNAME, PORT);

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);

        this.replyHandler = new ReplyHandler(this.in, this.out);

        new Thread(this.replyHandler).start();

        Terminal.info("Client is up at " + socket.getLocalSocketAddress());
        Terminal.info("Session established with server on " + socket.getRemoteSocketAddress());
    }

    public void disconnect() throws IOException {
        this.replyHandler.stop();
        this.socket.shutdownOutput();
        this.socket.shutdownInput();
        this.socket.close();
    }

    public static void welcome() {
        List<String> logo = Parse.readFile(Client.class.getResource("../art/logo.ascii").toString().split(":")[1]);
        Terminal.welcome(logo);
    }
}
