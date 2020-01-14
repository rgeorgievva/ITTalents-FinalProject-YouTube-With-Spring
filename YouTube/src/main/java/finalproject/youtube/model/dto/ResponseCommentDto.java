package finalproject.youtube.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "dd.MM.yyyy hh:mm:ss")
    private LocalDateTime timePosted;
    @NotNull
    private SmallUserDto  owner;
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
        this.owner = comment.getOwner().toSmallUserDto();
        this.setVideoId(comment.getVideoId());
        this.setLikes(comment.getLikes());
        this.setDislikes(comment.getDislikes());
        if(comment.getRepliedTo() != null){
            this.setRepliedTo(comment.getRepliedTo().toDto());
        }
    }
}
