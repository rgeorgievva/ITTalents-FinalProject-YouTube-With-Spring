package finalproject.youtube.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class RequestCommentDto {
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String text;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private long repliedTo;
}