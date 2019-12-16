package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public final class Downloader {

    private Downloader() {
    }

    public static byte[] toArray(final String path, final int offset, final int size) throws IOException {
        Path p = Paths.get(path);
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

    }

    public static void toFile(final String path, final int offset, final byte[] b) throws IOException {
        Path p = Paths.get(path);
        FileChannel fch = FileChannel.open(p, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        fch.write(ByteBuffer.wrap(b), offset);
        fch.close();
    }
}
