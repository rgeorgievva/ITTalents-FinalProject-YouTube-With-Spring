package finalproject.youtube.controller;

import finalproject.youtube.SessionManager;
import finalproject.youtube.exceptions.CommentException;
import finalproject.youtube.exceptions.VideoException;
import finalproject.youtube.model.dao.CommentDAO;
import finalproject.youtube.model.dao.VideoDAO;
import finalproject.youtube.model.entity.Comment;
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

    
    @GetMapping(value = "/{video_id}/comments/{comment_id}")
    public Comment getCommentById(HttpSession session,
                                  @PathVariable("video_id") int videoId,
                                  @PathVariable("comment_id") int commentId)
            throws VideoException, CommentException {
        if(videoDAO.getById(videoId) == null){
            throw new VideoException("There is no such video");
        }
        return commentDAO.getCommentById(commentId);
    }

    @PostMapping(value = "/{video_id}/comments/submit")
    public void submitComment(HttpSession session,
                              @RequestBody Comment comment,
                              @PathVariable("video_id") int videoId)
            throws CommentException, UserException, VideoException {
        if (!SessionManager.validateLogged(session)) {
            throw new CommentException("Please login to post a comment");
        }
        if(videoDAO.getById(videoId) == null){
            throw new VideoException("There is no such video");
        }
        comment.setTimePosted(LocalDateTime.now());
        comment.setVideoId(videoId);
        comment.setOwnerId((int) SessionManager.getLoggedUser(session).getId());
        commentDAO.addCommentToVideo(comment);
    }

    @PostMapping(value = "/{video_id}/comments/{parent_comment_id}/reply")
    public void submitReply(HttpSession session,
                            @RequestBody Comment reply,
                            @PathVariable("parent_comment_id") int parentCommentId,
                            @PathVariable("video_id") int videoId)
            throws CommentException, UserException, VideoException {
        if (!SessionManager.validateLogged(session)) {
            throw new CommentException("Please login to reply to a comment");
        }
        if(videoDAO.getById(videoId) == null){
            throw new VideoException("There is no such video");
        }
        Comment dbComment = commentDAO.getCommentById(parentCommentId);
        reply.setOwnerId(SessionManager.getLoggedUser(session).getId());
        reply.setVideoId(videoId);
        reply.setRepliedToId(parentCommentId);
        commentDAO.addReplyToComment(reply);
    }

    @PostMapping(value = "/{videoId}/comments/{commentId}/edit")
    public void editComment(HttpSession session,
                            @RequestBody Comment comment,
                            @PathVariable("videoId") int videoId,
                            @PathVariable("commentId") int commentId)
            throws CommentException, UserException, VideoException {
        if (!SessionManager.validateLogged(session)) {
            throw new CommentException("Please login to reply to a comment");
        }
        if(videoDAO.getById(videoId) == null){
            throw new VideoException("There is no such video");
        }
        Comment dbComment = commentDAO.getCommentById(commentId);
        if(dbComment.getOwnerId() != SessionManager.getLoggedUser(session).getId()){
            throw new CommentException("You are not the owner of this comment to edit it");
        }
        comment.setOwnerId(SessionManager.getLoggedUser(session).getId());
        comment.setId(commentId);
        comment.setTimePosted(LocalDateTime.now());
        commentDAO.editComment(comment);
    }

    @DeleteMapping(value = "/{videoId}/comments/{commentId}/delete")
    public void deleteComment(HttpSession session,
                              @PathVariable("videoId") int videoId,
                              @PathVariable("commentId") int commentId)
            throws CommentException, VideoException, UserException {
        if (!SessionManager.validateLogged(session)) {
            throw new CommentException("Please login to reply to a comment");
        }
        if(videoDAO.getById(videoId) == null){
            throw new VideoException("There is no such video");
        }
        Comment dbComment = commentDAO.getCommentById(commentId);
        if(dbComment.getOwnerId() != SessionManager.getLoggedUser(session).getId()){
            throw new CommentException("You are not the owner of this comment to edit it");
        }
        commentDAO.deleteComment(dbComment);
    }



}
