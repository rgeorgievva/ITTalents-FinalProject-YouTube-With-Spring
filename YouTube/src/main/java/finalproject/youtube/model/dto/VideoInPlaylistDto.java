package finalproject.youtube.model.dto;

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
    private String            ownerUsername;
    @NotNull
    private LocalDateTime     dateAdded;
}
