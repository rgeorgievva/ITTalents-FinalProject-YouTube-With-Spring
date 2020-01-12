package finalproject.youtube.service;

import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.dao.CommentDAO;
import finalproject.youtube.model.dto.RequestCommentDto;
import finalproject.youtube.model.dto.ResponseCommentDto;
import finalproject.youtube.model.dto.ResponseCommentWithRepliesDto;
import finalproject.youtube.model.dto.ResponseReplyDto;
import finalproject.youtube.model.pojo.Comment;
import finalproject.youtube.model.pojo.User;
import finalproject.youtube.model.pojo.Video;
import finalproject.youtube.model.repository.CommentRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
        //gets comment
        Optional <Comment> optionalComment = commentRepository.findById(commentId);
        if(!optionalComment.isPresent()){
            throw new NotFoundException("Comment with id="+commentId+" not found!");
        }
        Comment comment = optionalComment.get();
        return new ResponseCommentDto(comment) ;
    }

    @SneakyThrows
    public ResponseCommentDto submitComment(User user, RequestCommentDto requestCommentDto) {
        //check for videoID
        Video video = videoService.validateAndGetVideo(requestCommentDto.getVideoId());
        //get comment from dto
        Comment comment = new Comment(requestCommentDto);
        //check if there's a parent comment and if it is valid
        Long parentCommentId = requestCommentDto.getRepliedTo();
        if (parentCommentId != null) {
            Optional <Comment> optionalParentComment =
                    commentRepository.findById(parentCommentId);
            if (!optionalParentComment.isPresent()) {
                throw new NotFoundException("Parent comment with id=" + parentCommentId + " does not exist!");
            }
            Comment parentComment = optionalParentComment.get();
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
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        //check for comment availability
        if(!optionalComment.isPresent()){
            throw new NotFoundException("Comment with id "+commentId+" not found!");
        }
        Comment comment = optionalComment.get();
        //check for user authorization
        if(comment.getOwner().getId() != user.getId()){
            throw new AuthorizationException("You are not the owner of this comment to edit it");
        }
        //edits text
        comment.setText(requestCommentDto.getText());
        commentRepository.save(comment);
        return new ResponseCommentDto(comment);
    }

    @SneakyThrows
    public void deleteComment(User user, long commentId){
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        //check for comment availability
        if(!optionalComment.isPresent()){
            throw new NotFoundException("Comment with id "+commentId+" not found!");
        }
        Comment comment = optionalComment.get();
        //check for user authorization
        if(comment.getOwner().getId() != user.getId()){
            throw new AuthorizationException("You are not the owner of this comment to delete it");
        }
        //delete comment
        commentRepository.delete(comment);
    }

    @SneakyThrows
    public void likeComment(User user, long commentId){
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        //check for comment availability
        if(!optionalComment.isPresent()){
            throw new NotFoundException("Comment with id "+commentId+" not found!");
        }
        Comment comment = optionalComment.get();
        //like comment
        long currentUser = user.getId();
        commentDAO.react(currentUser, comment, LIKE_REACTION);
    }

    @SneakyThrows
    public void dislikeComment(User loggedUser, long commentId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        //check for comment availability
        if(!optionalComment.isPresent()){
            throw new NotFoundException("Comment with id "+commentId+" not found!");
        }
        Comment comment = optionalComment.get();
        //dislike comment
        long currentUser = loggedUser.getId();
        commentDAO.react(currentUser, comment, DISLIKE_REACTION);
    }

    public List <ResponseCommentWithRepliesDto> getAllCommentsForVideo(long videoId) {
        //check if video id is valid
        Video video = videoService.validateAndGetVideo(videoId);
        //check if there are any parent comments
        Optional<List <Comment>> commentList = commentRepository.findAllByVideoIdAndRepliedToIsNull(videoId);
        if(!commentList.isPresent()){
            return null;
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
        return response;
    }
}
