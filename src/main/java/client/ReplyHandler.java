package client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

import util.Downloader;
import view.Terminal;

public final class ReplyHandler implements Runnable {

    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean stopFlag;

    private static Handler data = (argv, out) -> {
        try {
            String fileID = argv[0];
            int offset = Integer.parseInt(argv[1]);
            byte[] b = Base64.getDecoder().decode(argv[2]);
            Downloader.toFile("./" + fileID + ".mp3", offset, b);
        } catch (ArrayIndexOutOfBoundsException e) {
            Terminal.error("Wrong number of arguments");
        } catch (IOException e) {
            Terminal.error("The download was interrupted unexpectedly");
        }
    };

    private static Handler request = (argv, out) -> {
        try {
            Path p = Paths.get(argv[1]);
            int chunkSize = Client.getMAXSIZE();
            int chunks = (int) (Files.size(p) / chunkSize) + 1;
            for (int i = 0; i < chunks; i++) {
                byte[] r = Downloader.toArray(argv[1], i * chunkSize, chunkSize);
                String enc = Base64.getEncoder().encodeToString(r);
                out.println("DATA " + argv[0] + " " + (i * chunkSize) + " " + enc);
                out.flush();
            }
            out.println("NOTIFICATION " + argv[0]);
            out.flush();
        } catch (FileNotFoundException e) {
            Terminal.error("File Not Found");
        } catch (IOException e) {
            Terminal.error("The upload was interrupted unexpectedly");
        }
    };

    private static Map<String, Handler> commands = Map.ofEntries(//
            Map.entry("DATA", data), //
            Map.entry("REQUEST", request) //
    );

    public ReplyHandler(final BufferedReader in, final PrintWriter out) {
        this.in = in;
        this.out = out;
        this.stopFlag = false;
    }

    public void stop() {
        this.stopFlag = true;
    }

    @Override
    public void run() {
        try {
            while (!stopFlag) {
                String[] content = in.readLine().split(":\\s?");
                String command = content[0].toUpperCase();

                if (commands.containsKey(command)) {
                    commands.get(command).execute(content[1].split(" "), this.out);
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
        } catch (Exception e) {
            Terminal.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
