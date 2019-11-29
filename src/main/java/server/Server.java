package server;

import model.FileShare;
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
        // criar o servidor
        try {
            this.socket = new ServerSocket(Server.PORT);
            Terminal.info("Server is up at " + this.socket.getLocalSocketAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // criar os workers
        for (int i = 0; i < N_WORKERS; i++) {
            new Thread(new Worker(this.requests, this.replies, this.model)).start();
        }

        // aceitar ligações
        int i = 1;
        while (true) {
            try {
                Terminal.info("Waiting for connection...");
                Socket clientServer = this.socket.accept();
                Terminal.info("Session " + i + " established on " + clientServer.getRemoteSocketAddress());
                new Thread(new Session(i, clientServer, this.requests, this.replies, this.model)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    public static void welcome() {
        Terminal.clear();
        List<String> logo = Parse.read("img/server.ascii");

        Terminal.show(logo);
    }
}
