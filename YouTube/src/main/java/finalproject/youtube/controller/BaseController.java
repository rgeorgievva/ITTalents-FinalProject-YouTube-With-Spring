package finalproject.youtube.controller;

import finalproject.youtube.exceptions.UserException;
import finalproject.youtube.exceptions.VideoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

public abstract class BaseController {

    @ExceptionHandler({UserException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleUserException(UserException e) {
        return e.getMessage();
    }

    @ExceptionHandler({VideoException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleVideoException(VideoException e) {
        return e.getMessage();
    }
}
