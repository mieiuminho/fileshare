package util;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class Downloader {

    private Downloader() {
    }

    public static byte[] toArray(final String path, final int offset, final int size) {
        Path p = Paths.get(path);
        try {
            FileInputStream fis = new FileInputStream(new File(path));

            if ((Files.size(p) - offset) < size) {
                int remaining = (int) (Files.size(p) - offset);
                byte[] r = new byte[remaining];
                fis.skip(offset);
                fis.readNBytes(r, 0, remaining);
                return r;
            }

            byte[] r = new byte[size];
            fis.skip(offset);
            fis.readNBytes(r, 0, size);
            return r;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void toFile(final String path, final byte[] b) {
        try {
            Path p = Paths.get(path);
            Files.write(p, b, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
