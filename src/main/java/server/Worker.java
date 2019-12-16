package server;

import exceptions.DuplicateSongException;
import exceptions.InexistentSongException;
import model.FileShare;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.BoundedBuffer;
import util.Downloader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.util.Arrays;

import static java.util.Map.entry;

public final class Worker implements Runnable {
    private BoundedBuffer<String> requests;
    private Map<Integer, PrintWriter> replies;
    private FileShare model;

    private static Logger log = LogManager.getLogger(Worker.class);

    private void notifyUsers(final String title, final String author) {
        String notification = "A new song is available. " + title + " by " + author
                + ". It may take a few moments for the song to become available to download";

        for (PrintWriter out : this.replies.values()) {
            synchronized (out) {
                out.println(notification);
                out.flush();
            }
        }
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private static Command upload = (argv, out, model) -> {
        String reply = null;
        try {
            int year = Integer.parseInt(argv[3]);
            List<String> tags = new ArrayList<>();
            for (int i = 5; i < argv.length; i++) {
                tags.add(argv[i]);
            }
            int id = model.upload(argv[0], argv[1], argv[2], year, tags);
            synchronized (out) {
                out.println("REQUEST: " + id + " " + argv[4]);
                out.flush();
            }
            reply = "REPLY: Began upload of the file (" + id + ")";
        } catch (ArrayIndexOutOfBoundsException e) {
            reply = "ERROR: wrong number of arguments";
        } catch (DuplicateSongException e) {
            reply = "ERROR: This file is already present in the system";
        } finally {
            synchronized (out) {
                out.println(reply);
                out.flush();
            }
        }
    };

    private static Command download = (argv, out, model) -> {
        String reply = null;
        try {
            int fileID = Integer.parseInt(argv[0]);
            long chunks = model.chunks(fileID);
            synchronized (out) {
                out.println("REPLY: Began download of " + fileID + "...");
                out.flush();
            }
            int chunkSize = model.getMaxSize();
            for (int i = 0; i < chunks; i++) {
                byte[] buf = model.download(fileID, i * chunkSize);
                String encoded = Base64.getEncoder().encodeToString(buf);
                synchronized (out) {
                    out.println("DATA: " + fileID + " " + (i * chunkSize) + " " + encoded);
                    out.flush();
                }
            }
            reply = "REPLY: The download of " + fileID + " has finished";
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
                out.flush();
            }
        }
    };

    private static Command search = (argv, out, model) -> {
        String reply = null;
        try {
            List<String> results = model.search(argv[0]);
            StringBuilder sb = new StringBuilder();
            for (String s : results) {
                sb.append(s + ";");
            }
            reply = "REPLY: " + sb.toString();
        } catch (ArrayIndexOutOfBoundsException e) {
            reply = "ERROR: wrong number of arguments";
        } finally {
            synchronized (out) {
                out.println(reply);
                out.flush();
            }
        }
    };

    private static Command data = (argv, out, model) -> {
        try {
            int fileID = Integer.parseInt(argv[0]);
            int offset = Integer.parseInt(argv[1]);
            byte[] b = Base64.getDecoder().decode(argv[2]);
            Downloader.toFile(model.getSongDir(fileID), offset, b);
        } catch (ArrayIndexOutOfBoundsException e) {
            synchronized (out) {
                out.println("ERROR: wrong number of arguments");
                out.flush();
            }
        } catch (IOException e) {
            synchronized (out) {
                out.println("ERROR: The upload was interrupted unexpectedly");
                out.flush();
            }
        } catch (InexistentSongException e) {
            synchronized (out) {
                out.println("ERROR: Couldn't write data");
                out.flush();
            }
        }
    };

    private static Command notify = (argv, out, model) -> {
        try {
            int fileID = Integer.parseInt(argv[0]);
            model.setAval(fileID);
        } catch (InexistentSongException e) {
            synchronized (out) {
                out.println("This upload was canceled. It exceded the time limit. Please try again");
                out.flush();
            }
        }
    };

    private void notifyUsers(final int id) throws InexistentSongException {
        String n = this.model.getNotif(id);
        for (PrintWriter p : this.replies.values()) {
            synchronized (p) {
                p.println(n);
                p.flush();
            }
        }
    }

    @SuppressWarnings("checkstyle:ConstantName")
    public static final Map<String, Command> commands = Map.ofEntries(//
            entry("upload", Worker.upload), //
            entry("download", Worker.download), //
            entry("search", Worker.search), //
            entry("data:", Worker.data), //
            entry("notification", Worker.notify)//
    );

    public Worker(final BoundedBuffer<String> requests, final Map<Integer, PrintWriter> replies,
            final FileShare model) {
        this.requests = requests;
        this.replies = replies;
        this.model = model;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String[] argv = this.requests.get().split("\\s+");
                int id = Integer.parseInt(argv[0]);
                @SuppressWarnings("checkstyle:AvoidInlineConditionals")
                String command = argv.length >= 2 ? argv[1].toLowerCase() : "HELP";
                PrintWriter out = this.replies.get(id);

                synchronized (out) {
                    if (Worker.commands.containsKey(command)) {
                        log.debug("(" + id + ") task: " + command);
                        if (command.equals("notification")) {
                            int argId = Integer.parseInt(argv[2]);
                            this.notifyUsers(argId);
                        }
                        Worker.commands.get(command).execute(Arrays.copyOfRange(argv, 2, argv.length), out, this.model);
                    } else {
                        if (!command.equals("help")) {
                            log.warn("(" + id + ") request not available: " + command);
                        } else
                            log.debug("(" + id + ") request for help");
                        String listOfCommands = "List of Available Commands:";

                        for (String cmd : Worker.commands.keySet()) {
                            listOfCommands += " " + cmd + ";";
                        }

                        out.println(listOfCommands);
                        out.flush();
                    }
                }
            } catch (InterruptedException | ArrayIndexOutOfBoundsException | InexistentSongException e) {
                e.printStackTrace();
            }
        }
    }

}
