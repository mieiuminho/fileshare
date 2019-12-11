package model;

import exceptions.AuthenticationException;
import exceptions.DuplicateUserException;
import exceptions.DuplicateSongException;
import exceptions.InexistentSongException;
import util.Downloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class FileShare {
    private static final int MEGA = 1048576;
    private static final int MAXDOWN = 5;
    private static final int MAXSIZE = MEGA;
    private static final String SONGDIR = System.getenv("FILESHARE_SERVER_DATA_DIR");

    private Map<String, User> users;
    private Map<Integer, Song> songs;
    private Lock lock;
    private Condition isCrowded;
    private int downloading;
    private int songCounter;

    public FileShare() {
        this.users = new HashMap<>();
        this.songs = new HashMap<>();
        this.lock = new ReentrantLock();
        this.isCrowded = this.lock.newCondition();
        this.downloading = 0;
        this.songCounter = 0;
    }

    public String getSongDir() {
        return FileShare.SONGDIR;
    }

    public void registerUser(final String username, final String password) throws DuplicateUserException {
        synchronized (this.users) {
            if (this.users.containsKey(username)) {
                throw new DuplicateUserException();
            }

            User newUser = new User(username, password);
            users.put(username, newUser);
        }
    }

    public void login(final String username, final String password) throws AuthenticationException {
        synchronized (this.users) {
            if (!this.users.containsKey(username)) {
                throw new AuthenticationException("Username isn't registered");
            }

            if (!this.users.get(username).validate(password)) {
                throw new AuthenticationException("Incorrect password");
            }
        }
    }

    public int upload(final String title, final String artist, final int year, final Collection<String> tags)
            throws DuplicateSongException {

        Song newSong = new Song(title, artist, year, tags, songCounter);

        synchronized (this.songs) {
            if (this.songs.containsKey(songCounter)) {
                throw new DuplicateSongException();
            }

            this.songs.put(songCounter, newSong);
        }
        this.lock.lock();
        int r = songCounter++;
        this.lock.unlock();
        return r;
    }

    public long sizeFile(final int id) throws InexistentSongException {
        synchronized (this.songs) {
            if (!this.songs.containsKey(id)) {
                throw new InexistentSongException();
            }
        }
        File file = new File(SONGDIR + id + ".mp3");
        return file.length();
    }

    public int chunks(final int id) throws InexistentSongException {
        long fileSize = sizeFile(id);
        return (int) (fileSize / MAXSIZE) + 1;
    }

    public int getMaxSize() {
        return FileShare.MAXSIZE;
    }

    public byte[] download(final int id, final int offset)
            throws InexistentSongException, InterruptedException, FileNotFoundException, IOException {

        while (this.downloading == MAXDOWN) {
            this.isCrowded.await();
        }

        long chunks = this.sizeFile(id) / MAXSIZE;

        if (offset == 0) {
            this.lock.lock();
            this.downloading++;
            this.lock.unlock();
            this.songs.get(id).download();
        }
        if (offset == chunks * MAXSIZE) {
            this.lock.lock();
            this.downloading--;
            this.lock.unlock();
        }

        if (this.downloading < MAXDOWN) {
            this.lock.lock();
            this.isCrowded.signalAll();
            this.lock.unlock();
        }

        String buffer = SONGDIR + id + ".mp3";

        return Downloader.toArray(buffer, offset, MAXSIZE);
    }

    public List<String> search(final String tag) {
        List<String> r = new ArrayList<>();

        synchronized (this.songs.values()) {
            for (Song s : this.songs.values()) {
                if (s.filter(tag))
                    r.add(s.toString());
            }
        }
        return r;
    }
}
