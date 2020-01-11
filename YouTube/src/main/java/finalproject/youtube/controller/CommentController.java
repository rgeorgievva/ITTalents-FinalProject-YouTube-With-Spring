package finalproject.youtube.controller;

import finalproject.youtube.SessionManager;
import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.dao.CommentDAO;
import finalproject.youtube.model.dto.RequestCommentDto;
import finalproject.youtube.model.dto.ResponseCommentWithRepliesDto;
import finalproject.youtube.model.dto.ResponseCommentDto;
import finalproject.youtube.model.dto.ResponseReplyDto;
import finalproject.youtube.model.entity.Comment;
import finalproject.youtube.model.entity.Video;
import finalproject.youtube.model.repository.CommentRepository;
import finalproject.youtube.service.VideoService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
public class CommentController extends BaseController{

    public static final int LIKE_REACTION    = 1;
    public static final int DISLIKE_REACTION = -1;

    @Autowired
    CommentDAO commentDAO;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    VideoService videoService;

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


    //todo change replied to - test
    @SneakyThrows
    @PostMapping(value = "/comments")
    public ResponseEntity<ResponseCommentDto> submitComment(HttpSession session,
                              @RequestBody RequestCommentDto requestCommentDto){
        //check for videoID
        Video video = videoService.validateAndGetVideo(requestCommentDto.getVideoId());
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to post a comment!");
        }
        //get comment from dto
        Comment comment = new Comment(requestCommentDto);
        //check if there's a parent comment and if it is valid
        Long parentCommentId = requestCommentDto.getRepliedTo();
        if( parentCommentId!= null) {
            Optional <Comment> optionalParentComment =
                    commentRepository.findById(parentCommentId);
            if(!optionalParentComment.isPresent()){
                throw new NotFoundException("Parent comment with id="+parentCommentId+" does not exist!");
            }
            Comment parentComment = optionalParentComment.get();
            //check if the parent comment is a reply to another comment
            if(parentComment.getRepliedTo() != null){
                //change the replied to to the very first parent comment (where replied to id is null)
                parentComment = parentComment.getRepliedTo();
            }
            comment.setRepliedTo(parentComment);
        }
        //sets up comment
        comment.setOwner(SessionManager.getLoggedUser(session));
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
        if(comment.getOwner().getId() != SessionManager.getLoggedUser(session).getId()){
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
        if(comment.getOwner().getId() != SessionManager.getLoggedUser(session).getId()){
            throw new AuthorizationException("You are not the owner of this comment to delete it");
        }
        //delete comment
        commentRepository.delete(comment);

        return new ResponseEntity <>("Comment with id="+commentId+" deleted!", HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping(value = "/comments/{commentId}/like")
    public void likeComment(HttpSession session,
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
        long currentUser = SessionManager.getLoggedUser(session).getId();
        commentDAO.react(currentUser, comment, LIKE_REACTION);
    }

    @SneakyThrows
    @GetMapping(value = "/comments/{commentId}/dislike")
    public void dislikeComment(HttpSession session,
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
        long currentUser = SessionManager.getLoggedUser(session).getId();
        commentDAO.react(currentUser, comment, DISLIKE_REACTION);
    }

    //todo get all comments for video - test
    @SneakyThrows
    @GetMapping(value = "/comments/from_video/{video_id}")
    public ResponseEntity<List <ResponseCommentWithRepliesDto>> getAllCommentsForVideo(
            @PathVariable("video_id") long videoId){

        //check if video id is valid
        Video video = videoService.validateAndGetVideo(videoId);
        //check if there are any parent comments
        Optional<List<Comment>> commentList = commentRepository.findAllByVideoIdAndRepliedToIsNull(videoId);
        if(!commentList.isPresent()){
            return new ResponseEntity <>(null, HttpStatus.OK);
        }
        //get all comments, if there are any
        List<Comment> parentComments = commentList.get();
        List<ResponseCommentWithRepliesDto> response = new ArrayList <>();
        //check if the comments have replies
        for (Comment p: parentComments) {
            Optional<List<Comment>> repliesToParent = commentRepository.findAllByRepliedToId(p.getId());
            //if there is a reply
            if(repliesToParent.isPresent()){
                List<Comment> replies = repliesToParent.get();
                List<ResponseReplyDto> repliesDtos = new ArrayList <>();
                for (Comment r: replies){
                 repliesDtos.add(r.toReplyDto());
                }
                response.add(new ResponseCommentWithRepliesDto(p,repliesDtos));
            }
            //if there are no replies
            else {
                response.add(new ResponseCommentWithRepliesDto(p));
            }
        }

        return new ResponseEntity <>(response, HttpStatus.OK);
    }
}
