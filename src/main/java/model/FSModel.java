package model;

import exceptions.DuplicateUserException;
import exceptions.DuplicateSongException;
import exceptions.InexistentSongException;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public final class FSModel {

    private Map<String, User> users;
    private Map<Integer, Song> songs;

    public FSModel() {
        this.users = new HashMap<>();
        this.songs = new HashMap<>();
    }

    public void registerUser(final String username, final String password) throws DuplicateUserException {

        if (this.users.containsKey(username)) {
            throw new DuplicateUserException();
        }

        User newUser = new User(username, password);
        users.put(username, newUser);
    }

    public boolean containsUser(final String username) {
        return this.users.containsKey(username);
    }

    public boolean matchPassword(final String username, final String password) {
        return this.users.get(username).validate(password);

    }

    public int upload(final String title, final String artist, final int year, final Collection<String> tags,
            final String path) throws DuplicateSongException {

        Song newSong = new Song(title, artist, year, tags, path);
        int id = newSong.hashCode();

        if (this.songs.containsKey(id)) {
            throw new DuplicateSongException();
        }

        this.songs.put(id, newSong);
        return id;
    }

    public void download(final int id) throws InexistentSongException {
        if (!this.songs.containsKey(id)) {
            throw new InexistentSongException();
        }
        this.songs.get(id).download();
    }

    public List<String> search(final String tag) {
        List<String> r = new ArrayList<>();
        for (Song s : this.songs.values()) {
            if (s.filter(tag))
                r.add(s.toString());
        }
        return r;
    }
}
