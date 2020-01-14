package finalproject.youtube.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import finalproject.youtube.model.pojo.Video;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoInPlaylistDto {
    @NotNull
    private long              id;
    @NotNull
    private String            title;
    @NotNull
    private String            videoUrl;
    @NotNull
    private String            thumbnailUrl;
    @NotNull
    private SmallUserDto      owner;
    @NotNull
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDateTime     dateAdded;
}
