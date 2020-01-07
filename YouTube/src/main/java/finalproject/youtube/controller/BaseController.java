package finalproject.youtube.controller;

import finalproject.youtube.exceptions.*;
import finalproject.youtube.model.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;

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


    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorDto handleBadRequest(BadRequestException e) {
        return new ErrorDto(e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                e.getClass().getName());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ErrorDto handleAuthorizationException(AuthorizationException e) {
        return new ErrorDto(e.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                LocalDateTime.now(),
                e.getClass().getName());
    }
}
