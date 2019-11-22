package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class Song {

    private int id;
    private String title;
    private String artist;
    private int year;
    private List<String> tags;
    private int numDownloads;

    public Song(final String title, final String artist, final int year, final Collection<String> c,
            final String path) {
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.tags = new ArrayList<>(c);
        this.id = this.hashCode();

        this.numDownloads = 0;
    }

    public int getId() {
        return id;
    }

    public void download() {

        // [WIP] DOWNLOAD FILE
        this.numDownloads++;
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
        sb.append("Song ID: " + this.id + "\n");
        sb.append("Title: " + this.title + "\n");
        sb.append("Artist: " + this.artist + "\n");
        sb.append("Release Year: " + this.year + "\n");
        sb.append("Number of Downloads: " + this.numDownloads + "\n");
        sb.append("TAGS\n");
        for (String s : this.tags) {
            sb.append("\t" + s + "\n");
        }
        return sb.toString();
    }
}
