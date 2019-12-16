package server;

import model.FileShare;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.TimerTask;

public final class Cleaner extends TimerTask {
    private FileShare model;

    public Cleaner(final FileShare fs) {
        this.model = fs;
    }

    public void run() {
        List<String> toRemove;
        System.out.println("Cleaning...");
        try {
            toRemove = model.cleanup();
            for (String s : toRemove) {
                Files.deleteIfExists(Paths.get(s));
                System.out.println("Deleted " + s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
