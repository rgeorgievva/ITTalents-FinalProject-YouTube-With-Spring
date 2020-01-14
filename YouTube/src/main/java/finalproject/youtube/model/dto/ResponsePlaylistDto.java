package finalproject.youtube.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import finalproject.youtube.model.pojo.Playlist;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Setter
@Getter
public class ResponsePlaylistDto {
    @NotNull
    private long id;
    @NotNull
    private String        title;
    @NotNull
    private SmallUserDto        owner;
    @NotNull
    @JsonFormat(pattern = "dd.MM.yyyy hh:mm:ss")
    private LocalDateTime dateCreated;
    private List<VideoInPlaylistDto> videos;

    public ResponsePlaylistDto(Playlist playlist) {
        this.setId(playlist.getId());
        this.setDateCreated(playlist.getDateCreated());
        this.setDateCreated(playlist.getDateCreated());
        this.owner = playlist.getOwner().toSmallUserDto();
        this.setTitle(playlist.getTitle());
    }

    public ResponsePlaylistDto(Playlist playlist, List<VideoInPlaylistDto> videos) {
        this(playlist);
        this.setVideos(videos);
    }
}
