package finalproject.youtube.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class SmallPlaylistDto {
    @NotNull
    private long          id;
    @NotNull
    private String        title;
    @NotNull
    private SmallUserDto  owner;
    @NotNull
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDateTime dateCreated;
}

