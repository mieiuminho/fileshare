package server;

import model.FileShare;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.BoundedBuffer;
import util.Parse;
import view.Terminal;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Server {
    private static final int PORT = Integer.parseInt(System.getenv("FILESHARE_SERVER_PORT"));
    private static final int N_WORKERS = 5;
    private static final int REQUESTS_MAX_SIZE = 10;
    private static Logger log = LogManager.getLogger(Server.class);

    private ServerSocket socket;
    private BoundedBuffer<String> requests;
    private Map<Integer, PrintWriter> replies;
    private FileShare model;

    public Server() {
        this.requests = new BoundedBuffer<>(Server.REQUESTS_MAX_SIZE);
        this.replies = new HashMap<>();
        this.model = new FileShare();
    }

    public static void main(final String[] args) {
        Server.welcome();
        new Server().startUp();
    }

    public void startUp() {
        log.debug("Working Directory " + System.getProperty("user.dir"));

        // criar o servidor
        try {
            this.socket = new ServerSocket(Server.PORT);
            log.info("Server is up at " + this.socket.getLocalSocketAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // criar os workers
        for (int i = 0; i < N_WORKERS; i++) {
            new Thread(new Worker(this.requests, this.replies, this.model)).start();
        }

        // aceitar ligações
        int id = N_WORKERS + 1;
        while (true) {
            try {
                log.info("Waiting for connection...");
                Socket clientServer = this.socket.accept();
                new Thread(new Session(id, clientServer, this.requests, this.replies, this.model)).start();
                log.debug("Session " + id + " accepted connection");
            } catch (IOException e) {
                e.printStackTrace();
            }
            id++;
        }
    }

    public static void welcome() {
        Terminal.clear();
        List<String> logo = Parse.readFile(Server.class.getResource("../art/server.ascii").toString().split(":")[1]);

        Terminal.show(logo);
    }
}
