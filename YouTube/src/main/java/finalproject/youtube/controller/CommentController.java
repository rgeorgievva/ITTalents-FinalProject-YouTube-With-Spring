package finalproject.youtube.controller;

import finalproject.youtube.utils.SessionManager;
import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.model.dto.RequestCommentDto;
import finalproject.youtube.model.dto.ResponseCommentWithRepliesDto;
import finalproject.youtube.model.dto.ResponseCommentDto;
import finalproject.youtube.service.CommentService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;


@RestController
public class CommentController extends BaseController{

    @Autowired
    CommentService commentService;

    @SneakyThrows
    @GetMapping(value = "/comments/{comment_id}")
    public ResponseEntity <ResponseCommentDto> getCommentById(@PathVariable("comment_id") long commentId){
        return new ResponseEntity(commentService.getCommentById(commentId), HttpStatus.OK) ;
    }

    @SneakyThrows
    @PostMapping(value = "/comments")
    public ResponseEntity<ResponseCommentDto> submitComment(HttpSession session,
                                                            @RequestBody RequestCommentDto requestCommentDto) {
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to post a comment!");
        }
        return new ResponseEntity<>( commentService.submitComment(SessionManager.getLoggedUser(session),
                requestCommentDto), HttpStatus.OK);
    }

    @SneakyThrows
    @PutMapping(value = "/comments/{commentId}")
    public ResponseEntity<ResponseCommentDto> editComment(HttpSession session,
                            @RequestBody RequestCommentDto requestCommentDto,
                            @PathVariable("commentId") long commentId){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to edit comment!");
        }
        return new ResponseEntity(commentService.editComment(SessionManager.getLoggedUser(session),
                requestCommentDto,commentId), HttpStatus.OK);
    }

    @SneakyThrows
    @DeleteMapping(value = "/comments/{commentId}")
    public ResponseEntity<String> deleteComment(HttpSession session,
                              @PathVariable("commentId") long commentId){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to delete comment!");
        }
        commentService.deleteComment(SessionManager.getLoggedUser(session),commentId);
        return new ResponseEntity <>("Comment with id="+commentId+" deleted!", HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping(value = "/comments/{commentId}/like")
    public void likeComment(HttpSession session,
                            @PathVariable("commentId") long commentId){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to like comment!");
        }
        commentService.likeComment(SessionManager.getLoggedUser(session), commentId);
    }

    @SneakyThrows
    @GetMapping(value = "/comments/{commentId}/dislike")
    public void dislikeComment(HttpSession session,
                            @PathVariable("commentId") long commentId){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to dislike comment!");
        }
       commentService.dislikeComment(SessionManager.getLoggedUser(session), commentId);
    }

}
