package finalproject.youtube.model.dto;

import finalproject.youtube.model.entity.Playlist;
import finalproject.youtube.model.entity.Video;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class ResponsePlaylistDto {
    @NotNull
    private String        title;
    @NotNull
    private String        ownerUsername;
    @NotNull
    private LocalDateTime dateCreated;
    private List<Video> videos;



    public ResponsePlaylistDto(Playlist playlist) {
        this.setDateCreated(playlist.getDateCreated());
        this.setOwnerUsername(playlist.getOwner().getUsername());
        this.setTitle(playlist.getTitle());
    }

    public ResponsePlaylistDto(Playlist playlist, List<Video> videos) {
        this(playlist);
        this.setVideos(videos);
    }
}
