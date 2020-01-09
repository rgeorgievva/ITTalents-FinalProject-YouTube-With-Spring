package finalproject.youtube.model.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class ResponseCommentWithRepliesDto {
    @NotNull
    private String        text;
    @NotNull
    private LocalDateTime timePosted;
    @NotNull
    private long          ownerId;
    @NotNull
    private long          videoId;
    List <ResponseCommentWithRepliesDto> replies;

}
