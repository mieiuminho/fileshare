package exceptions;

public class DuplicateUserException extends Exception {

    public DuplicateUserException() {
        super("Username is already in use");
    }
}
