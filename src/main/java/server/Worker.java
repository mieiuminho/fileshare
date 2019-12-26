package server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import exceptions.DuplicateSongException;
import exceptions.InexistentSongException;

import model.FileShare;
import util.BoundedBuffer;
import util.Command;
import util.Downloader;

public final class Worker implements Runnable {
    private BoundedBuffer<String> requests;
    private Map<Integer, PrintWriter> replies;
    private FileShare model;

    private static Logger log = LogManager.getLogger(Worker.class);

    public Worker(final BoundedBuffer<String> requests, final Map<Integer, PrintWriter> replies,
            final FileShare model) {
        this.requests = requests;
        this.replies = replies;
        this.model = model;
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private void upload(final String[] argv, final PrintWriter out) {
        String reply = null;
        try {
            int year = Integer.parseInt(argv[3]);
            List<String> tags = new ArrayList<>();
            for (int i = 4; i < argv.length; i++) {
                tags.add(argv[i]);
            }
            int id = this.model.upload(argv[1], argv[2], year, tags);
            synchronized (out) {
                out.println("REQUEST: " + id + " " + argv[0]);
            }
            reply = "REPLY: Began upload of the file (" + id + ")";
        } catch (ArrayIndexOutOfBoundsException e) {
            reply = "ERROR: wrong number of arguments";
        } catch (DuplicateSongException e) {
            reply = "ERROR: This file is already present in the system";
        } finally {
            synchronized (out) {
                out.println(reply);
            }
        }
    }

    private void download(final String[] argv, final PrintWriter out) {
        String reply = null;
        try {
            int fileID = Integer.parseInt(argv[0]);
            String fileName = argv[1];
            long chunks = this.model.chunks(fileID);
            synchronized (out) {
                out.println("REPLY: Began download of " + fileName + "...");
            }
            int chunkSize = this.model.getMaxSize();
            for (int i = 0; i < chunks; i++) {
                byte[] buf = this.model.download(fileID, i * chunkSize);
                String encoded = Base64.getEncoder().encodeToString(buf);
                synchronized (out) {
                    out.println("DATA: " + fileName + " " + (i * chunkSize) + " " + encoded);
                }
            }
            reply = "REPLY: The download of " + fileName + " has finished";
        } catch (InexistentSongException e) {
            reply = "ERROR: Invalid file ID";
        } catch (InterruptedException e) {
            reply = "ERROR: The download was interrupted. The file may not be available at this time";
        } catch (ArrayIndexOutOfBoundsException e) {
            reply = "ERROR: wrong number of arguments";
        } catch (FileNotFoundException e) {
            reply = "ERROR: This file is currently not available.";
        } catch (IOException e) {
            reply = "ERROR: The download was interrupted unexpectedly";
        } finally {
            synchronized (out) {
                out.println(reply);
            }
        }
    }

    @SuppressWarnings("checkstyle:AvoidInlineConditionals")
    private void search(final String[] argv, final PrintWriter out) {
        String reply = null;
        try {
            List<String> results = this.model.search(argv[0]);
            StringBuilder sb = new StringBuilder();
            for (String s : results) {
                sb.append(s + "; ");
            }
            reply = sb.toString().length() == 0 ? "No results for this search." : sb.toString();
        } catch (ArrayIndexOutOfBoundsException e) {
            reply = "ERROR: wrong number of arguments";
        } finally {
            synchronized (out) {
                out.println("REPLY:  " + reply);
            }
        }
    }

    private void data(final String[] argv, final PrintWriter out) {
        try {
            int fileID = Integer.parseInt(argv[0]);
            int offset = Integer.parseInt(argv[1]);
            byte[] b = Base64.getDecoder().decode(argv[2]);
            Downloader.toFile(this.model.getSongDir(fileID), offset, b);
        } catch (ArrayIndexOutOfBoundsException e) {
            synchronized (out) {
                out.println("ERROR: wrong number of arguments");
            }
        } catch (IOException e) {
            synchronized (out) {
                out.println("ERROR: The upload was interrupted unexpectedly");
            }
        } catch (InexistentSongException e) {
            synchronized (out) {
                out.println("ERROR: Couldn't write data");
            }
        }
    }

    private void notify(final String[] argv, final PrintWriter out) {
        try {
            int fileID = Integer.parseInt(argv[0]);
            this.model.setAvailable(fileID);
            String notification = this.model.getNotification(fileID);
            for (PrintWriter p : this.replies.values()) {
                synchronized (p) {
                    p.println("NOTIFICATION: " + notification);
                }
            }
        } catch (InexistentSongException e) {
            synchronized (out) {
                out.println("ERROR: This upload was canceled. It exceeded the time limit. Please try again.");
            }
        }
    }

    public static final List<String> OPTIONS = Arrays.asList("upload", "download", "search", "data", "notification");

    private final Map<String, Command> commands = Map.ofEntries(//
            Map.entry("upload", this::upload), //
            Map.entry("download", this::download), //
            Map.entry("search", this::search), //
            Map.entry("data", this::data), //
            Map.entry("notification", this::notify) //
    );

    @Override
    public void run() {
        while (true) {
            try {
                String[] argv = this.requests.get().split("\\s+");
                int id = Integer.parseInt(argv[0]);
                PrintWriter out = this.replies.get(id);
                this.commands.get(argv[1].toLowerCase()).execute(Arrays.copyOfRange(argv, 2, argv.length), out);
            } catch (InterruptedException | ArrayIndexOutOfBoundsException e) {
                log.error(e.getMessage());
            }
        }
    }
}
