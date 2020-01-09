package finalproject.youtube.controller;

import finalproject.youtube.SessionManager;
import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.dao.CommentDAO;
import finalproject.youtube.model.dto.RequestCommentDto;
import finalproject.youtube.model.dto.ResponseCommentDto;
import finalproject.youtube.model.entity.Comment;
import finalproject.youtube.model.entity.User;
import finalproject.youtube.model.repository.CommentRepository;
import finalproject.youtube.model.repository.VideoRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Optional;


@RestController
public class CommentController extends BaseController{

    @Autowired
    CommentDAO commentDAO;
    @Autowired
    CommentRepository commentRepository;


    @SneakyThrows
    @GetMapping(value = "/comments/{comment_id}")
    public ResponseEntity <ResponseCommentDto> getCommentById(@PathVariable("comment_id") long commentId){
        //gets comment
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if(!optionalComment.isPresent()){
            throw new NotFoundException("Comment with id="+commentId+" not found!");
        }
        Comment comment = optionalComment.get();
        return new ResponseEntity(new ResponseCommentDto(comment),HttpStatus.OK) ;
    }

    @SneakyThrows
    @PostMapping(value = "/comments/")
    public ResponseEntity<ResponseCommentDto> submitComment(HttpSession session,
                              @RequestBody RequestCommentDto requestCommentDto){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to post a comment!");
        }
        //check if there's a parent comment and if it is valid
        Comment comment = new Comment(requestCommentDto);
        Long parentCommentId = requestCommentDto.getRepliedToCommentId();
        if(parentCommentId != null){
            Optional<Comment> optionalParentComment = commentRepository.findById(parentCommentId);
            if(!optionalParentComment.isPresent()){
                throw new NotFoundException("Parent comment with id="+ parentCommentId +" does not exist!");
            }
            comment.setRepliedTo(optionalParentComment.get());
        }
        //sets up comment
        comment.setOwnerId(SessionManager.getLoggedUser(session).getId());

        commentRepository.save(comment);
        return new ResponseEntity<>(new ResponseCommentDto(comment), HttpStatus.OK);
    }

    @SneakyThrows
    @PutMapping(value = "/comments/{commentId}")
    public ResponseEntity<ResponseCommentDto> editComment(HttpSession session,
                            @RequestBody RequestCommentDto requestCommentDto,
                            @PathVariable("commentId") long commentId){
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        //check for comment availability
        if(!optionalComment.isPresent()){
            throw new NotFoundException("Comment with id "+commentId+" not found!");
        }
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to edit comment!");
        }
        Comment comment = optionalComment.get();
        //check for user authorization
        if(comment.getOwnerId() != SessionManager.getLoggedUser(session).getId()){
            throw new AuthorizationException("You are not the owner of this comment to edit it");
        }
        //edits text
        comment.setText(requestCommentDto.getText());
        commentRepository.save(comment);

        return new ResponseEntity <>(new ResponseCommentDto(comment), HttpStatus.OK);
    }

    @SneakyThrows
    @DeleteMapping(value = "/comments/{commentId}")
    public ResponseEntity<String> deleteComment(HttpSession session,
                              @PathVariable("commentId") long commentId){
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        //check for comment availability
        if(!optionalComment.isPresent()){
            throw new NotFoundException("Comment with id "+commentId+" not found!");
        }
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to delete comment!");
        }
        Comment comment = optionalComment.get();
        //check for user authorization
        if(comment.getOwnerId() != SessionManager.getLoggedUser(session).getId()){
            throw new AuthorizationException("You are not the owner of this comment to delete it");
        }
        //delete comment
        commentRepository.delete(comment);

        return new ResponseEntity <>("Comment with id="+commentId+" deleted!", HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping(value = "/comments/{commentId}/like")
    public ResponseEntity<String> likeComment(HttpSession session,
                            @PathVariable("commentId") long commentId){
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        //check for comment availability
        if(!optionalComment.isPresent()){
            throw new NotFoundException("Comment with id "+commentId+" not found!");
        }
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to like comment!");
        }
        Comment comment = optionalComment.get();
        //like comment
        User currentUser = SessionManager.getLoggedUser(session);
        String message = commentDAO.likeComment(currentUser, comment);

        return new ResponseEntity <>(message, HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping(value = "/comments/{commentId}/dislike")
    public ResponseEntity<String> dislikeComment(HttpSession session,
                            @PathVariable("commentId") long commentId){
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        //check for comment availability
        if(!optionalComment.isPresent()){
            throw new NotFoundException("Comment with id "+commentId+" not found!");
        }
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to dislike comment!");
        }
        Comment comment = optionalComment.get();
        //dislike comment
        User currentUser = SessionManager.getLoggedUser(session);
        String message = commentDAO.dislikeComment(currentUser, comment);

        return new ResponseEntity <>(message, HttpStatus.OK);
    }
}
