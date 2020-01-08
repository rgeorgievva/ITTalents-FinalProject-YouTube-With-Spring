package finalproject.youtube.controller;

import finalproject.youtube.SessionManager;
import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.dao.CommentDAO;
import finalproject.youtube.model.dao.VideoDAO;
import finalproject.youtube.model.entity.Comment;
import finalproject.youtube.model.entity.User;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;


@RestController
public class CommentController {

    @Autowired
    CommentDAO commentDAO;
    @Autowired
    VideoDAO videoDAO;


    @SneakyThrows
    @GetMapping(value = "/{video_id}/comments/{comment_id}")
    public Comment getCommentById(HttpSession session,
                                  @PathVariable("video_id") int videoId,
                                  @PathVariable("comment_id") int commentId){
        if(videoDAO.getById(videoId) == null){
            throw new NotFoundException("There is no such video");
        }
        return commentDAO.getCommentById(commentId);
    }

    @SneakyThrows
    @PostMapping(value = "/{video_id}/comments/submit")
    public void submitComment(HttpSession session,
                              @RequestBody Comment comment,
                              @PathVariable("video_id") int videoId) {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to post a comment");
        }
        if(videoDAO.getById(videoId) == null){
            throw new NotFoundException("There is no such video");
        }
        comment.setTimePosted(LocalDateTime.now());
        comment.setVideoId(videoId);
        comment.setOwnerId((int) SessionManager.getLoggedUser(session).getId());
        commentDAO.addCommentToVideo(comment);
    }

    @SneakyThrows
    @PostMapping(value = "/{video_id}/comments/{parent_comment_id}/reply")
    public void submitReply(HttpSession session,
                            @RequestBody Comment reply,
                            @PathVariable("parent_comment_id") int parentCommentId,
                            @PathVariable("video_id") int videoId){
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to reply to a comment");
        }
        if(videoDAO.getById(videoId) == null){
            throw new NotFoundException("There is no such video");
        }
        Comment dbComment = commentDAO.getCommentById(parentCommentId);
        reply.setOwnerId(SessionManager.getLoggedUser(session).getId());
        reply.setVideoId(videoId);
        reply.setRepliedToId(parentCommentId);
        commentDAO.addReplyToComment(reply);
    }

    @SneakyThrows
    @PostMapping(value = "/{videoId}/comments/{commentId}/edit")
    public void editComment(HttpSession session,
                            @RequestBody Comment comment,
                            @PathVariable("videoId") int videoId,
                            @PathVariable("commentId") int commentId){
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to edit a comment");
        }
        if(videoDAO.getById(videoId) == null){
            throw new NotFoundException("There is no such video");
        }
        Comment dbComment = commentDAO.getCommentById(commentId);
        if(dbComment.getOwnerId() != SessionManager.getLoggedUser(session).getId()){
            throw new AuthorizationException("You are not the owner of this comment to edit it");
        }
        comment.setOwnerId(SessionManager.getLoggedUser(session).getId());
        comment.setId(commentId);
        comment.setTimePosted(LocalDateTime.now());
        commentDAO.editComment(comment);
    }

    @SneakyThrows
    @DeleteMapping(value = "/{videoId}/comments/{commentId}/delete")
    public void deleteComment(HttpSession session,
                              @PathVariable("videoId") int videoId,
                              @PathVariable("commentId") int commentId){
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to delete a comment");
        }
        if(videoDAO.getById(videoId) == null){
            throw new NotFoundException("There is no such video");
        }
        Comment dbComment = commentDAO.getCommentById(commentId);
        if(dbComment.getOwnerId() != SessionManager.getLoggedUser(session).getId()){
            throw new AuthorizationException("You are not the owner of this comment to edit it");
        }
        commentDAO.deleteComment(dbComment);
    }

    @SneakyThrows
    @PostMapping(value = "/{videoId}/comments/{commentId}/like")
    public void likeComment(HttpSession session,
                            @PathVariable("videoId") int videoId,
                            @PathVariable("commentId") int commentId){
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to like a comment");
        }
        if(videoDAO.getById(videoId) == null){
            throw new NotFoundException("There is no such video");
        }
        Comment dbComment = commentDAO.getCommentById(commentId);
        User currentUser = SessionManager.getLoggedUser(session);
        commentDAO.likeComment(currentUser, dbComment);
    }

    @SneakyThrows
    @PostMapping(value = "/{videoId}/comments/{commentId}/dislike")
    public void dislikeComment(HttpSession session,
                            @PathVariable("videoId") int videoId,
                            @PathVariable("commentId") int commentId){
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to reply to a comment");
        }
        if(videoDAO.getById(videoId) == null){
            throw new NotFoundException("There is no such video");
        }
        Comment dbComment = commentDAO.getCommentById(commentId);
        User currentUser = SessionManager.getLoggedUser(session);
        commentDAO.dislikeComment(currentUser, dbComment);
    }


}
