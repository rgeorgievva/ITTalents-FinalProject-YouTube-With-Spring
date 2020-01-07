package finalproject.youtube.controller;

import finalproject.youtube.SessionManager;
import finalproject.youtube.exceptions.CommentException;
import finalproject.youtube.exceptions.UserException;
import finalproject.youtube.exceptions.VideoException;
import finalproject.youtube.model.dao.CommentDAO;
import finalproject.youtube.model.dao.VideoDAO;
import finalproject.youtube.model.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;


@RestController
public class CommentController {

    @Autowired
    CommentDAO commentDAO;
    @Autowired
    VideoDAO videoDAO;

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
        if(commentDAO.getCommentById(parentCommentId) == null){
            throw new CommentException("There is no such comment with id=" + parentCommentId);
        }
        reply.setOwnerId(SessionManager.getLoggedUser(session).getId());
        reply.setVideoId(videoId);
        reply.setRepliedToId(parentCommentId);
        commentDAO.addReplyToComment(reply);
    }



}
