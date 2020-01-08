package finalproject.youtube.model.dto;

import finalproject.youtube.model.entity.Comment;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

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

        this.createFromComment(comment);
        if(comment.getRepliedTo() != null){
            this.createReply(comment);
        }
    }

    private void createFromComment(Comment comment){
        this.setText(comment.getText());
        this.setTimePosted(comment.getTimePosted());
        this.setOwnerId(comment.getOwnerId());
        this.setVideoId(comment.getVideoId());
    }

    private void createReply(Comment comment){
        this.setRepliedTo(comment.getRepliedTo());
    }
}
