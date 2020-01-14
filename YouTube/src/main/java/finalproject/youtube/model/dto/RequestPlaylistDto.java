package finalproject.youtube.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class RequestPlaylistDto {
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String title;
}
