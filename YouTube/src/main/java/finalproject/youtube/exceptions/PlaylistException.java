package finalproject.youtube.exceptions;

public class PlaylistException extends Exception {

    public PlaylistException(String message) {
        super(message);
    }

    public PlaylistException(String message, Throwable cause) {
        super(message, cause);
    }
}
