package finalproject.youtube.controller;

import finalproject.youtube.exceptions.UserException;
import finalproject.youtube.exceptions.VideoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public abstract class BaseController {

    @ExceptionHandler({UserException.class})
    public ResponseEntity<String> handleUserException(UserException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({VideoException.class})
    public ResponseEntity<String> handleVideoException(VideoException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
