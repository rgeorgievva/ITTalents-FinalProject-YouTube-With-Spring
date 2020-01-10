package finalproject.youtube.model.dto;

import finalproject.youtube.model.entity.Comment;
import finalproject.youtube.model.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


//todo BIG TODO ADD LIKES AND DISLIKES

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
    private Comment repliedTo;

    public ResponseCommentDto(Comment comment){
        this.setId(comment.getId());
        this.setText(comment.getText());
        this.setTimePosted(comment.getTimePosted());
        this.owner = comment.getOwner().toNoPasswordUserDto();
        this.setVideoId(comment.getVideoId());
        if(comment.getRepliedTo() != null){
            this.setRepliedTo(comment.getRepliedTo());
        }
    }
}
