package finalproject.youtube.service;

import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.exceptions.BadRequestException;
import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.dao.CommentDAO;
import finalproject.youtube.model.dto.RequestCommentDto;
import finalproject.youtube.model.dto.ResponseCommentDto;
import finalproject.youtube.model.pojo.Comment;
import finalproject.youtube.model.pojo.User;
import finalproject.youtube.model.pojo.Video;
import finalproject.youtube.model.repository.CommentRepository;
import finalproject.youtube.utils.Validator;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class CommentService {

    public static final int LIKE_REACTION    = 1;
    public static final int DISLIKE_REACTION = -1;

    @Autowired
    CommentDAO        commentDAO;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    VideoService      videoService;

    @SneakyThrows
    public ResponseCommentDto getCommentById(long commentId){
        return new ResponseCommentDto(validateAndGetComment(commentId)) ;
    }

    @SneakyThrows
    public ResponseCommentDto submitComment(User user, long videoId, RequestCommentDto requestCommentDto) {
        //check for video existence
        Video video = videoService.validateAndGetVideo(videoId);
        Comment comment = new Comment(requestCommentDto, videoId);
        //check if there's a parent comment and if it is valid
        Long parentCommentId = requestCommentDto.getRepliedTo();
        if (parentCommentId != null) {
            Comment parentComment = validateAndGetComment(parentCommentId);
            //check if the parent comment is a reply to another comment
            if (parentComment.getRepliedTo() != null) {
                //change the replied to to the very first parent comment (where replied to id is null)
                parentComment = parentComment.getRepliedTo();
            }
            comment.setRepliedTo(parentComment);
        }
        //sets up comment
        comment.setOwner(user);
        commentRepository.save(comment);
        return new ResponseCommentDto(comment);
    }

    @SneakyThrows
    public ResponseCommentDto editComment(User user, RequestCommentDto requestCommentDto, long commentId){
        Comment comment = validateAndGetComment(commentId);
        //check for user authorization
        if(comment.getOwner().getId() != user.getId()){
            throw new AuthorizationException("You are not the owner of this comment to edit it");
        }
        //check if the new text is like before
        if(requestCommentDto.getText().equals(comment.getText())){
            throw new BadRequestException("You haven't made any changes to the text");
        }
        //edits text
        comment.setText(requestCommentDto.getText());
        commentRepository.save(comment);
        return new ResponseCommentDto(comment);
    }

    @SneakyThrows
    public void deleteComment(User user, long commentId){
        Comment comment = validateAndGetComment(commentId);
        //check for user authorization
        if(comment.getOwner().getId() != user.getId()){
            throw new AuthorizationException("You are not the owner of this comment to delete it");
        }
        //delete comment
        commentRepository.delete(comment);
    }

    @SneakyThrows
    public void likeComment(User user, long commentId){
        Comment comment = validateAndGetComment(commentId);
        //like comment
        long currentUser = user.getId();
        commentDAO.react(currentUser, comment, LIKE_REACTION);
    }

    @SneakyThrows
    public void dislikeComment(User user, long commentId){
        Comment comment = validateAndGetComment(commentId);
        //dislike comment
        long currentUser = user.getId();
        commentDAO.react(currentUser, comment, DISLIKE_REACTION);
    }

    public Comment validateAndGetComment(long commentId){
        Validator.validateCommentId(commentId);
        //checks comment existence
        Optional <Comment> optionalComment = commentRepository.findById(commentId);
        if(!optionalComment.isPresent()){
            throw new NotFoundException("Comment with id="+commentId+" not found!");
        }
        return optionalComment.get();
    }
}
