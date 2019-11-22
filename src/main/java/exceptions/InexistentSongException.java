package exceptions;

public class InexistentSongException extends Exception {

    public InexistentSongException() {
        super("Could not find Song");
    }
}
