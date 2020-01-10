package finalproject.youtube.model.dto;

import finalproject.youtube.model.entity.Playlist;
import finalproject.youtube.model.entity.Video;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

//todo get videos to be dto

@Setter
@Getter
public class ResponsePlaylistDto {
    @NotNull
    private long id;
    @NotNull
    private String        title;
    @NotNull
    private NoPasswordUserDto        owner;
    @NotNull
    private LocalDateTime dateCreated;
    private List<Video> videos;

    public ResponsePlaylistDto(Playlist playlist) {
        this.setId(playlist.getId());
        this.setDateCreated(playlist.getDateCreated());
        this.owner = playlist.getOwner().toNoPasswordUserDto();
        this.setTitle(playlist.getTitle());
    }

    public ResponsePlaylistDto(Playlist playlist, List<Video> videos) {
        this(playlist);
        this.setVideos(videos);
    }
}
