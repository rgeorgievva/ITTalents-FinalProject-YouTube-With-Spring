package finalproject.youtube.controller;

import finalproject.youtube.utils.SessionManager;
import finalproject.youtube.model.dto.RequestCommentDto;
import finalproject.youtube.model.dto.ResponseCommentDto;
import finalproject.youtube.service.CommentService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
@Validated
public class CommentController extends BaseController{

    @Autowired
    CommentService commentService;

    @SneakyThrows
    @GetMapping(value = "/comments/{commentId}")
    public ResponseEntity <ResponseCommentDto> getCommentById(@PathVariable("commentId") long commentId){
        return new ResponseEntity(commentService.getCommentById(commentId), HttpStatus.OK) ;
    }

   @SneakyThrows
    @PostMapping(value = "/videos/{videoId}/comments")
    public ResponseEntity<ResponseCommentDto> submitComment(HttpSession session,
                                                            @PathVariable(value = "videoId") long videoId,
                                                            @RequestBody @Valid RequestCommentDto requestCommentDto){
        return new ResponseEntity<>( commentService.submitComment(SessionManager.getLoggedUser(session),
                videoId, requestCommentDto), HttpStatus.OK);
    }

    @SneakyThrows
    @PutMapping(value = "/comments/{commentId}")
    public ResponseEntity<ResponseCommentDto> editComment(HttpSession session,
                            @PathVariable("commentId") long commentId,
                            @RequestParam(value = "text", defaultValue = "") String text){
        return new ResponseEntity(commentService.editComment(SessionManager.getLoggedUser(session),
                text,commentId), HttpStatus.OK);
    }

    @SneakyThrows
    @DeleteMapping(value = "/comments/{commentId}")
    public ResponseEntity<String> deleteComment(HttpSession session,
                              @PathVariable("commentId") long commentId){
        commentService.deleteComment(SessionManager.getLoggedUser(session),commentId);
        return new ResponseEntity <>("Comment with id="+commentId+" deleted!", HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping(value = "/comments/{commentId}/like")
    public void likeComment(HttpSession session,
                            @PathVariable("commentId") long commentId){
        commentService.likeComment(SessionManager.getLoggedUser(session), commentId);
    }

    @SneakyThrows
    @GetMapping(value = "/comments/{commentId}/dislike")
    public void dislikeComment(HttpSession session,
                            @PathVariable("commentId") long commentId){
       commentService.dislikeComment(SessionManager.getLoggedUser(session), commentId);
    }

}
