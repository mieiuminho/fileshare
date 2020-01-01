package model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import exceptions.AuthenticationException;
import exceptions.DuplicateUserException;
import exceptions.DuplicateSongException;
import exceptions.InexistentSongException;

import util.Downloader;
import util.Parse;

public final class FileShare implements Serializable {
    private static final int KB = 1024;
    private static final int MAXSIZE = Integer.parseInt(System.getenv("FILESHARE_MAX_MEMORY_SIZE")) * KB;
    private static final int MAXDOWN = 5;
    private static final String DATA_DIR = System.getenv("FILESHARE_SERVER_DATA_DIR");
    private static final String MODEL_DATA = DATA_DIR + "model.ser";
    private static final int UPLOAD_TIME_LIMIT = 20;

    private Map<String, User> users;
    private Map<Integer, Song> songs;
    private Lock lock;
    private Condition isCrowded;
    private int downloading;
    private int songCounter;

    public FileShare() {
        this.users = new HashMap<>();
        this.songs = new HashMap<>();
        this.lock = new ReentrantLock(true);
        this.isCrowded = this.lock.newCondition();
        this.downloading = 0;
        this.songCounter = 0;
    }

    public String getSongDir(final int id) throws InexistentSongException {

        if (!this.songs.containsKey(id)) {
            throw new InexistentSongException();
        }

        return DATA_DIR + id;
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

        Song newSong = new Song(songCounter, title, artist, year, FileShare.UPLOAD_TIME_LIMIT, tags);

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
            if (!this.songs.containsKey(id) || !this.songs.get(id).isAvailable()) {
                throw new InexistentSongException();
            }
        }

        String filePath = this.getSongDir(id);

        return new File(filePath).length();
    }

    public int chunks(final int id) throws InexistentSongException {
        long fileSize = sizeFile(id);
        return (int) (fileSize / MAXSIZE) + 1;
    }

    public int getMaxSize() {
        return FileShare.MAXSIZE;
    }

    public byte[] download(final int id, final int offset)
            throws InexistentSongException, InterruptedException, IOException {

        while (this.downloading == MAXDOWN && offset == 0) {
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

        String buffer = this.getSongDir(id);

        return Downloader.toArray(buffer, offset, MAXSIZE);
    }

    public void setAvailable(final int id) throws InexistentSongException {
        if (!this.songs.containsKey(id)) {
            throw new InexistentSongException();
        }

        this.songs.get(id).setAvailable(true);
    }

    public String getNotification(final int id) throws InexistentSongException {
        if (!this.songs.containsKey(id)) {
            throw new InexistentSongException();
        }
        Song s = this.songs.get(id);
        return "A new song is available. " + s.getTitle() + " by " + s.getArtist();
    }

    public List<String> search(final String tag) {
        List<String> r = new ArrayList<>();

        synchronized (this.songs.values()) {
            for (Song s : this.songs.values()) {
                if (s.isAvailable() && s.filter(tag))
                    r.add(s.toString());
            }
        }

        return r;
    }

    public List<String> cleanup() throws InexistentSongException {
        List<String> toBeRemoved = new ArrayList<>();
        for (Song s : this.songs.values()) {
            if (!s.isAvailable() && s.hasExpired()) {
                String path = this.getSongDir(s.getId());
                toBeRemoved.add(path);
                synchronized (this.songs.values()) {
                    this.songs.remove(s.getId());
                }
            }
        }
        return toBeRemoved;
    }

    public void save() throws IOException {
        Parse.saveObject(this, MODEL_DATA);
    }

    public FileShare load() throws IOException, ClassNotFoundException {
        return (FileShare) Parse.loadObject(MODEL_DATA);
    }
}
