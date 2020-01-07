package finalproject.youtube.controller;

import finalproject.youtube.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

public abstract class BaseController {

    @ExceptionHandler({UserException.class})
    public ResponseEntity<String> handleUserException(UserException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({VideoException.class})
    public ResponseEntity<String> handleVideoException(VideoException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({CommentException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleCommentException(CommentException e){ return e.getMessage();}

    @ExceptionHandler({PlaylistException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handlePlaylistException(PlaylistException e){return e.getMessage();}

}
