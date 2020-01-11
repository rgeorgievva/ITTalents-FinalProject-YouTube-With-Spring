package finalproject.youtube.model.dto;

import finalproject.youtube.model.entity.Comment;
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
    private LocalDateTime timePosted;
    @NotNull
    private String ownerUsername;
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
        this.setOwnerUsername(comment.getOwner().getUsername());
    }
}
