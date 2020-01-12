package finalproject.youtube.model.dto;

import finalproject.youtube.model.pojo.Comment;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Setter
@Getter
public class ResponseCommentDto {

    @NotNull
    private long id;
    @NotNull
    private String        text;
    @NotNull
    private LocalDateTime timePosted;
    @NotNull
    private NoPasswordUserDto owner;
    @NotNull
    private long          videoId;
    @NotNull
    private int likes;
    @NotNull
    private int dislikes;
    private ResponseCommentDto repliedTo;

    public ResponseCommentDto(Comment comment){
        this.setId(comment.getId());
        this.setText(comment.getText());
        this.setTimePosted(comment.getTimePosted());
        this.owner = comment.getOwner().toNoPasswordUserDto();
        this.setVideoId(comment.getVideoId());
        this.setLikes(comment.getLikes());
        this.setDislikes(comment.getDislikes());
        if(comment.getRepliedTo() != null){
            this.setRepliedTo(comment.getRepliedTo().toDto());
        }
    }
}
