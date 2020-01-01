package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.Date;
import java.util.List;

import sun.misc.Signal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.FileShare;
import util.HashPriorityBuffer;
import util.Parse;
import util.RequestsBuffer;
import view.Terminal;

public final class Server {
    private static final String HOSTNAME = System.getenv("FILESHARE_SERVER_HOSTNAME");
    private static final int PORT = Integer.parseInt(System.getenv("FILESHARE_SERVER_PORT"));
    private static final int REQUESTS_MAX_SIZE = Integer.parseInt(System.getenv("FILESHARE_SERVER_REQUEST_LIMIT"));
    private static final int N_WORKERS = Integer.parseInt(System.getenv("FILESHARE_SERVER_NUMBER_OF_THREADS"));
    private static final long CLOCK = 1800000; // 30 * 60 * 1000
    private static Logger log = LogManager.getLogger(Server.class);

    private ServerSocket socket;
    private RequestsBuffer<String> requests;
    private Map<Integer, PrintWriter> replies;
    private FileShare model;

    public Server() {
        this.requests = new HashPriorityBuffer(Server.REQUESTS_MAX_SIZE);
        this.replies = new HashMap<>();
        this.model = new FileShare();
    }

    public static void main(final String[] args) {
        Server.welcome();
        new Server().startUp();
    }

    @SuppressWarnings("checkstyle:magicnumber")
    public void startUp() {
        log.debug("Working Directory " + System.getProperty("user.dir"));

        // handler to Ctrl + C
        Signal.handle(new Signal("INT"), this::saveModel);

        // load previous model data
        this.loadModel();

        new Timer().scheduleAtFixedRate(new Cleaner(this.model), new Date(), Server.CLOCK);

        // criar o servidor
        try {
            this.socket = new ServerSocket();
            this.socket.bind(new InetSocketAddress(HOSTNAME, PORT));
            log.info("Server is up at " + this.socket.getLocalSocketAddress());
        } catch (IOException e) {
            log.fatal(e.getMessage());
        }

        // criar os workers
        for (int i = 0; i < N_WORKERS; i++) {
            new Thread(new Worker(this.requests, this.replies, this.model)).start();
        }

        // aceitar ligações
        int id = N_WORKERS + 2; // workers + timer
        while (true) {
            try {
                log.info("Waiting for connection...");
                Socket clientServer = this.socket.accept();
                new Thread(new Session(id, clientServer, this.requests, this.replies, this.model)).start();
                log.debug("Session " + id + " accepted connection");
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            id++;
        }
    }

    public void saveModel(final Signal signal) {
        try {
            this.model.save();
            log.info("Saved model data successfully");
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            System.exit(0);
        }
    }

    public void loadModel() {
        try {
            this.model = this.model.load();
            log.info("Loaded model data successfully");
        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage());
        }
    }

    public static void welcome() {
        Terminal.clear();
        List<String> logo = Parse.readFile(Server.class.getResource("../art/server.ascii").toString().split(":")[1]);

        Terminal.show(logo);
    }
}
