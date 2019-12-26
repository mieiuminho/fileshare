package client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

import util.Downloader;
import util.Filter;
import view.Terminal;

public final class ReplyHandler implements Runnable {
    private static final String DOWNLOADS_DIR = System.getenv("FILESHARE_CLIENT_DOWNLOADS_DIR");

    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean stopFlag;

    public ReplyHandler(final BufferedReader in, final PrintWriter out) {
        this.in = in;
        this.out = out;
        this.stopFlag = false;
    }

    public void stop() {
        this.stopFlag = true;
    }

    private void data(String[] argv) {
        try {
            String fileName = argv[0];
            int offset = Integer.parseInt(argv[1]);
            byte[] b = Base64.getDecoder().decode(argv[2]);
            Downloader.toFile(DOWNLOADS_DIR + fileName, offset, b);
        } catch (ArrayIndexOutOfBoundsException e) {
            Terminal.error("Wrong number of arguments");
        } catch (IOException e) {
            Terminal.error("The download was interrupted unexpectedly");
        }
    }

    private void request(String[] argv) {
        try {
            Path p = Paths.get(argv[1]);
            int chunkSize = Client.getMAXSIZE();
            int chunks = (int) (Files.size(p) / chunkSize) + 1;
            for (int i = 0; i < chunks; i++) {
                byte[] r = Downloader.toArray(argv[1], i * chunkSize, chunkSize);
                String enc = Base64.getEncoder().encodeToString(r);
                this.out.println("DATA " + argv[0] + " " + (i * chunkSize) + " " + enc);
            }
            this.out.println("NOTIFICATION " + argv[0]);
        } catch (FileNotFoundException e) {
            Terminal.error("File Not Found");
        } catch (IOException e) {
            Terminal.error("The upload was interrupted unexpectedly");
        }
    }

    private final Map<String, Filter> commands = Map.ofEntries(//
            Map.entry("DATA", this::data), //
            Map.entry("REQUEST", this::request) //
    );

    @Override
    public void run() {
        try {
            while (!stopFlag) {
                String[] content = in.readLine().split(":\\s?");
                String command = content[0].toUpperCase();

                if (commands.containsKey(command)) {
                    commands.get(command).execute(content[1].split(" "));
                } else {
                    switch (command) {
                        case "ERROR":
                            Terminal.error(content[1]);
                            break;
                        case "REPLY":
                            Terminal.response(content[1]);
                            break;
                        case "NOTIFICATION":
                            Terminal.notification(content[1]);
                            break;
                        default:
                            Terminal.response(content[0], content[1].split(";"));
                    }
                }
            }
        } catch (SocketException e) {
            System.exit(0);
        } catch (IOException e) {
            Terminal.error(e.getMessage());
        }
    }
}
