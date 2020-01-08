package finalproject.youtube.model.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class RequestPlaylistDto {
    @NotNull
    private String title;
}
