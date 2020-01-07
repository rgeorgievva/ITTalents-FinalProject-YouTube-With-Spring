package finalproject.youtube.exceptions;

public class AuthorizationException extends Exception {

    public AuthorizationException(String message){
        super(message);
    }

    public AuthorizationException(){
        super("You must be logged in to use this service!");
    }
}
