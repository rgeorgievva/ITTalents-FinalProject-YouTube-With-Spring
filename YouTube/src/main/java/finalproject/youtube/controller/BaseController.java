package finalproject.youtube.controller;

import finalproject.youtube.exceptions.*;
import finalproject.youtube.model.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.SQLException;
import java.time.LocalDateTime;

public abstract class BaseController {

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

    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ErrorDto handleAuthorizationException(AuthorizationException e) {
        return new ErrorDto(e.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                LocalDateTime.now(),
                e.getClass().getName());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorDto handleNotFoundException(NotFoundException e) {
        return new ErrorDto(e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now(),
                e.getClass().getName());
    }

    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto handleSQLExceptions(SQLException e){
        ErrorDto errorDTO = new ErrorDto(
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now(),
                e.getClass().getName());
        return errorDTO;
    }
}
