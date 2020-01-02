package finalproject.youtube.exceptions;

public class VideoException extends Exception {

    public VideoException(String message) {
        super(message);
    }

    public VideoException(String message, Throwable cause) {
        super(message, cause);
    }
}
