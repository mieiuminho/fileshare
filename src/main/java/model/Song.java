package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class Song {

    private int id;
    private boolean aval;
    private String fileName;
    private String title;
    private String artist;
    private int year;
    private List<String> tags;
    private int numDownloads;
    private Lock lock;
    private LocalDateTime timeLimit;

    public Song(final int id, final String fileName, final String title, final String artist, final int year,
            final int timeLimit, final Collection<String> c) {
        this.id = id;
        this.aval = false;
        this.fileName = fileName;
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.tags = new ArrayList<>(c);
        this.lock = new ReentrantLock();
        this.numDownloads = 0;
        this.timeLimit = LocalDateTime.now().plusMinutes(timeLimit);
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return this.title;
    }

    public boolean isAval() {
        return this.aval;
    }

    public boolean hasExpired() {
        return LocalDateTime.now().isAfter(this.timeLimit);
    }

    public void setAval(final boolean b) {
        this.aval = b;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getArtist() {
        return this.artist;
    }

    public void download() {
        this.lock.lock();
        this.numDownloads++;
        this.lock.unlock();
    }

    public boolean filter(final String tag) {
        return this.tags.contains(tag);
    }

    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Song song = (Song) o;
        return this.id == song.getId();
    }

    public int hashCode() {
        return Objects.hash(title, artist, year);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Song ID-" + this.id + "-");
        sb.append("Title-" + this.title + "-");
        sb.append("Artist-" + this.artist + "-");
        sb.append("Release Year-" + this.year + "-");
        sb.append("Number of Downloads-" + this.numDownloads + "-");
        sb.append("TAGS-");
        for (String s : this.tags) {
            sb.append(s + "-");
        }
        return sb.toString();
    }
}
