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
    private boolean available;
    private String title;
    private String artist;
    private int year;
    private List<String> tags;
    private int numDownloads;
    private Lock lock;
    private LocalDateTime timeLimit;

    public Song(final int id, final String title, final String artist, final int year, final int timeLimit,
            final Collection<String> c) {
        this.id = id;
        this.available = false;
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.tags = new ArrayList<>(c);
        this.lock = new ReentrantLock(true);
        this.numDownloads = 0;
        this.timeLimit = LocalDateTime.now().plusMinutes(timeLimit);
    }

    public int getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public boolean hasExpired() {
        return LocalDateTime.now().isAfter(this.timeLimit);
    }

    public void setAvailable(final boolean b) {
        this.available = b;
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
        sb.append("ID " + this.id + " | ");
        sb.append("TITLE " + this.title + " | ");
        sb.append("ARTIST " + this.artist + " | ");
        sb.append("RELEASE YEAR " + this.year + " | ");
        sb.append("NUMBER OF DOWNLOADS " + this.numDownloads + " | ");
        sb.append("TAGS ");
        for (String s : this.tags) {
            sb.append(s + " ");
        }
        return sb.toString();
    }
}
