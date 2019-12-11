package client;

import util.Downloader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import static java.util.Map.entry;

public final class ClientWorker {
    private PrintWriter out;

    private static ClientCommand data = (argv, out) -> {
        try {
            String fileID = argv[0];
            int offset = Integer.parseInt(argv[1]);
            byte[] b = Base64.getDecoder().decode(argv[2]);
            Downloader.toFile("./" + fileID + ".mp3", offset, b);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("ERROR: wrong number of arguments");
        } catch (IOException e) {
            System.out.println("ERROR: The download was interrupted unexpectedly");
        }
    };
    private static ClientCommand request = (argv, out) -> {
        try {
            Path p = Paths.get(argv[1]);
            int chunkSize = Client.getMAXSIZE();
            int chunks = (int) (Files.size(p) / chunkSize) + 1;
            for (int i = 0; i < chunks; i++) {
                byte[] r = Downloader.toArray(argv[1], i * chunkSize, chunkSize);
                String enc = Base64.getEncoder().encodeToString(r);
                out.println("DATA: " + argv[0] + " " + (i * chunkSize) + " " + enc);
                out.flush();
            }
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: File Not Found");
        } catch (IOException e) {
            System.out.println("ERROR: The upload was interrupted unexpectedly");
        }
    };

    private static Map<String, ClientCommand> commands = Map.ofEntries(//
            entry("data:", ClientWorker.data), //
            entry("request:", ClientWorker.request)//
    );

    public ClientWorker(final PrintWriter out) {
        this.out = out;
    }

    public void run(final String str) {
        String[] argv = str.split(" ");
        if (ClientWorker.commands.containsKey(argv[0].toLowerCase())) {
            ClientWorker.commands.get(argv[0].toLowerCase()).execute(Arrays.copyOfRange(argv, 1, argv.length),
                    this.out);
        }
    }
}
