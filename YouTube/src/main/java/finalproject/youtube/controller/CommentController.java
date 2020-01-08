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


@RestController
public class CommentController extends BaseController{

    @Autowired
    CommentDAO commentDAO;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    VideoRepository videoRepository;


    //todo test
    @SneakyThrows
    @GetMapping(value = "/{video_id}/comments/{comment_id}")
    public ResponseEntity <ResponseCommentDto> getCommentById(HttpSession session,
                                                              @PathVariable("video_id") long videoId,
                                                              @PathVariable("comment_id") long commentId){
        //checks for video availability
        if(!videoRepository.existsVideoById(videoId)){
            throw new NotFoundException("Video with id "+videoId+" not found!");
        }
        //gets comment
        if(!commentRepository.existsCommentById(commentId)){
            throw new NotFoundException("Comment with id="+commentId+" not found!");
        }
        Comment comment = commentRepository.getCommentById(commentId);
        return new ResponseEntity(new ResponseCommentDto(comment),HttpStatus.OK) ;
    }

    //todo test
    @SneakyThrows
    @PostMapping(value = "/{video_id}/comments/submit")
    public ResponseEntity<ResponseCommentDto> submitComment(HttpSession session,
                              @RequestBody RequestCommentDto requestCommentDto,
                              @PathVariable("video_id") long videoId) {
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to post a comment!");
        }
        //checks for video availability
        if(!videoRepository.existsVideoById(videoId)){
            throw new NotFoundException("Video with id "+videoId+" not found!");
        }
        //sets up comment
        Comment comment = new Comment(requestCommentDto, videoId);
        comment.setOwnerId(SessionManager.getLoggedUser(session).getId());
        commentRepository.save(comment);
        return new ResponseEntity<>(new ResponseCommentDto(comment), HttpStatus.OK);
    }

    //todo test
    @SneakyThrows
    @PostMapping(value = "/{video_id}/comments/{parent_comment_id}/reply")
    public ResponseEntity<ResponseCommentDto> submitReply(HttpSession session,
                            @RequestBody RequestCommentDto requestReplyDto,
                            @PathVariable("parent_comment_id") long parentCommentId,
                            @PathVariable("video_id") long videoId){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to post a reply to comment!");
        }
        //checks for video availability
        if(!videoRepository.existsVideoById(videoId)){
            throw new NotFoundException("Video with id "+videoId+" not found!");
        }
        //check if parent comment exists
        if(!commentRepository.existsCommentById(parentCommentId)){
            throw new NotFoundException("Parent comment with id "+parentCommentId+" not found");
        }
        //sets up reply
        Comment parentComment =  commentRepository.getCommentById(parentCommentId);
        Comment comment = new Comment(requestReplyDto, videoId, parentComment);
        comment.setOwnerId(SessionManager.getLoggedUser(session).getId());
        commentRepository.save(comment);
        return new ResponseEntity <>(new ResponseCommentDto(comment), HttpStatus.OK);

    }

    //todo test
    @SneakyThrows
    @PostMapping(value = "/{videoId}/comments/{commentId}/edit")
    public ResponseEntity<ResponseCommentDto> editComment(HttpSession session,
                            @RequestBody RequestCommentDto requestCommentDto,
                            @PathVariable("videoId") long videoId,
                            @PathVariable("commentId") long commentId){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to edit comment!");
        }
        //checks for video availability
        //checks for video availability
        if(!videoRepository.existsVideoById(videoId)){
            throw new NotFoundException("Video with id "+videoId+" not found!");
        }
        //check for comment availability
        if(!commentRepository.existsCommentById(commentId)){
            throw new NotFoundException("Comment with id "+commentId+" not found!");
        }
        Comment comment = commentRepository.getCommentById(commentId);
        //check for user authorization
        if(comment.getOwnerId() != SessionManager.getLoggedUser(session).getId()){
            throw new AuthorizationException("You are not the owner of this comment to edit it");
        }
        //changes text
        comment.setText(requestCommentDto.getText());
        commentRepository.save(comment);

        return new ResponseEntity <>(new ResponseCommentDto(comment), HttpStatus.OK);
    }

    //todo test
    @SneakyThrows
    @DeleteMapping(value = "/{videoId}/comments/{commentId}/delete")
    public ResponseEntity<String> deleteComment(HttpSession session,
                              @PathVariable("videoId") long videoId,
                              @PathVariable("commentId") long commentId){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to delete comment!");
        }
        //checks for video availability
        if(!videoRepository.existsVideoById(videoId)){
            throw new NotFoundException("Video with id "+videoId+" not found!");
        }
        //check for comment availability
        if(!commentRepository.existsCommentById(commentId)){
            throw new NotFoundException("Comment with id "+commentId+" not found!");
        }
        Comment comment = commentRepository.getCommentById(commentId);
        //check for user authorization
        if(comment.getOwnerId() != SessionManager.getLoggedUser(session).getId()){
            throw new AuthorizationException("You are not the owner of this comment to delete it");
        }
        //delete comment
        commentRepository.delete(comment);

        return new ResponseEntity <>("Comment with id="+commentId+" deleted!", HttpStatus.OK);
    }

    //todo string??? test
    @SneakyThrows
    @PostMapping(value = "/{videoId}/comments/{commentId}/like")
    public ResponseEntity<String> likeComment(HttpSession session,
                            @PathVariable("videoId") long videoId,
                            @PathVariable("commentId") long commentId){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to like a comment!");
        }
        //checks for video availability
        if(!videoRepository.existsVideoById(videoId)){
            throw new NotFoundException("Video with id "+videoId+" not found!");
        }
        //check for comment availability
        if(!commentRepository.existsCommentById(commentId)){
            throw new NotFoundException("Comment with id "+commentId+" not found!");
        }
        //like comment
        Comment comment = commentRepository.getCommentById(commentId);
        User currentUser = SessionManager.getLoggedUser(session);
        commentDAO.likeComment(currentUser, comment);

        return new ResponseEntity <>("Comment liked!", HttpStatus.OK);
    }

    //todo string?? test
    @SneakyThrows
    @PostMapping(value = "/{videoId}/comments/{commentId}/dislike")
    public ResponseEntity<String> dislikeComment(HttpSession session,
                            @PathVariable("videoId") long videoId,
                            @PathVariable("commentId") long commentId){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to dislike a comment!");
        }
        //checks for video availability
        if(!videoRepository.existsVideoById(videoId)){
            throw new NotFoundException("Video with id "+videoId+" not found!");
        }
        //check for comment availability
        if(!commentRepository.existsCommentById(commentId)){
            throw new NotFoundException("Comment with id "+commentId+" not found!");
        }
        //dislike comment
        Comment comment = commentRepository.getCommentById(commentId);
        User currentUser = SessionManager.getLoggedUser(session);
        commentDAO.dislikeComment(currentUser, comment);

        return new ResponseEntity <>("Comment disliked!", HttpStatus.OK);
    }
}
