package finalproject.youtube.model.dto;

import finalproject.youtube.model.entity.Comment;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


//todo BIG TODO ADD LIKES AND DISLIKES

@Setter
@Getter
public class ResponseCommentDto {

    @NotNull
    private String        text;
    @NotNull
    private LocalDateTime timePosted;
    @NotNull
    private long ownerId;
    @NotNull
    private long videoId;
    private Comment repliedTo;

    public ResponseCommentDto(Comment comment){

        this.setText(comment.getText());
        this.setTimePosted(comment.getTimePosted());
        this.setOwnerId(comment.getOwnerId());
        this.setVideoId(comment.getVideoId());
        if(comment.getRepliedTo() != null){
            this.setRepliedTo(comment.getRepliedTo());
        }
    }
}
