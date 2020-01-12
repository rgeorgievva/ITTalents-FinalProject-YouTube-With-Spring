package finalproject.youtube.model.dto;

import finalproject.youtube.model.pojo.Comment;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ResponseCommentWithRepliesDto {
    @NotNull
    private long id;
    @NotNull
    private String        text;
    @NotNull
    private LocalDateTime timePosted;
    @NotNull
    private NoPasswordUserDto owner;
    @NotNull
    private int likes;
    @NotNull
    private int dislikes;
    private List <ResponseReplyDto> replies;

    public ResponseCommentWithRepliesDto(Comment comment){
        this.setId(comment.getId());
        this.setText(comment.getText());
        this.setTimePosted(comment.getTimePosted());
        this.owner = comment.getOwner().toNoPasswordUserDto();
        this.setLikes(comment.getLikes());
        this.setDislikes(comment.getDislikes());
    }

    public ResponseCommentWithRepliesDto(Comment comment, List<ResponseReplyDto> replies){
        this(comment);
        this.setReplies(replies);
    }
}
