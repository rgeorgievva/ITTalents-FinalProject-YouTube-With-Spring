package finalproject.youtube.model.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class RequestCommentDto {
    @NotNull
    private String text;
    @NotNull
    private long videoId;
    private Long repliedTo;
}
