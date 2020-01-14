package finalproject.youtube.model.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class RequestCommentDto {
    @NotNull
    private String text;
    private Long repliedTo;
}
