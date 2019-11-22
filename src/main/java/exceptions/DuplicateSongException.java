package exceptions;

public class DuplicateSongException extends Exception {

    public DuplicateSongException() {
        super("Song already present in the system");
    }
}
