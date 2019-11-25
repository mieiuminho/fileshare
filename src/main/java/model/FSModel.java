package model;

import exceptions.DuplicateUserException;
import exceptions.DuplicateSongException;
import exceptions.InexistentSongException;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class FSModel {

    private static final int MAXDOWN = 5;

    private Map<String, User> users;
    private Map<Integer, Song> songs;
    private Lock lock;
    private Condition isCrowded;
    private int downloading;

    public FSModel() {
        this.users = new HashMap<>();
        this.songs = new HashMap<>();
        this.lock = new ReentrantLock();
        this.isCrowded = this.lock.newCondition();
        this.downloading = 0;
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

    public boolean containsUser(final String username) {
        synchronized (this.users) {
            return this.users.containsKey(username);
        }
    }

    public boolean matchPassword(final String username, final String password) {
        synchronized (this.users) {
            return this.users.get(username).validate(password);
        }
    }

    public int upload(final String title, final String artist, final int year, final Collection<String> tags,
            final String path) throws DuplicateSongException {

        Song newSong = new Song(title, artist, year, tags, path);
        int id = newSong.hashCode();

        synchronized (this.songs) {
            if (this.songs.containsKey(id)) {
                throw new DuplicateSongException();
            }

            this.songs.put(id, newSong);
        }
        return id;
    }

    public void download(final int id) throws InexistentSongException, InterruptedException {

        while (this.downloading == MAXDOWN) {
            this.isCrowded.await();
        }

        synchronized (this.songs) {
            if (!this.songs.containsKey(id)) {
                throw new InexistentSongException();
            }
        }

        this.lock.lock();
        this.downloading++;
        this.lock.unlock();

        this.songs.get(id).download();

        this.lock.lock();
        this.downloading--;
        this.lock.unlock();

        if (this.downloading < MAXDOWN)
            this.isCrowded.signalAll();
    }

    public List<String> search(final String tag) {
        List<String> r = new ArrayList<>();

        synchronized (this.songs) {
            for (Song s : this.songs.values()) {
                if (s.filter(tag))
                    r.add(s.toString());
            }
        }
        return r;
    }
}
