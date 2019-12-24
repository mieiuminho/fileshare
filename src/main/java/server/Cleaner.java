package server;

import model.FileShare;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TimerTask;

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
    }
}
