package finalproject.youtube.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import finalproject.youtube.model.pojo.Comment;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class ResponseReplyDto {
    @NotNull
    private long          id;
    @NotNull
    private String        text;
    @NotNull
    @JsonFormat(pattern = "dd.MM.yyyy hh:mm:ss")
    private LocalDateTime timePosted;
    @NotNull
    private SmallUserDto owner;
    @NotNull
    private int likes;
    @NotNull
    private int dislikes;

    public ResponseReplyDto(Comment comment) {
        this.setId(comment.getId());
        this.setText(comment.getText());
        this.setTimePosted(comment.getTimePosted());
        this.setLikes(comment.getLikes());
        this.setDislikes(comment.getDislikes());
        this.setOwner(comment.getOwner().toSmallUserDto());
    }
}
