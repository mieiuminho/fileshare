package server;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.FileShare;

public final class Cleaner extends TimerTask {
    private static Logger log = LogManager.getLogger(Cleaner.class);

    private FileShare model;

    public Cleaner(final FileShare fs) {
        this.model = fs;
    }

    public void run() {
        log.info("Start cleaning...");
        try {
            for (String file : model.cleanup()) {
                Files.deleteIfExists(Paths.get(file));
                log.debug("Deleted " + file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.trace("Done cleaning");
    }
}
