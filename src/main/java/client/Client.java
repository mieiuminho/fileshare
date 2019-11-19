package client;

import server.Connection;
import util.Parse;
import view.Terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public final class Client {

    private Client() {
    }

    @SuppressWarnings("checkstyle:InnerAssignment")
    public static void main(final String[] args) {
        Client.welcome();

        try {
            Socket socket = new Socket(Connection.hostname, Connection.port);
            Terminal.info("Connection established with server on " + Connection.hostname + ":" + Connection.port);

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
        List<String> logo = Parse.read("img/client.ascii");

        Terminal.show(logo);
    }
}
