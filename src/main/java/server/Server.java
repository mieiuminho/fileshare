package server;

import model.FSModel;
import util.Parse;
import view.Terminal;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public final class Server {

    private static ServerSocket socket;

    private Server() {
    }

    public static void main(final String[] args) {
        Server.welcome();

        FSModel model = new FSModel();

        try {
            Server.socket = new ServerSocket(Session.port);
            Terminal.info("Server on port " + Session.port);

        } catch (IOException e) {
            e.printStackTrace();
        }

        int i = 1;
        while (true) {
            try {
                Terminal.info("Waiting for connection...");
                Socket clientServer = Server.socket.accept();
                Terminal.info("Session " + i + " established!");
                new Thread(new Session(clientServer, i, model)).start();
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
