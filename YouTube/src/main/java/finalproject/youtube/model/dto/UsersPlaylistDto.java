package finalproject.youtube.model.dto;

import finalproject.youtube.model.pojo.Playlist;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UsersPlaylistDto {

    private long id;
    private String title;
    private LocalDateTime dateCreated;

    public UsersPlaylistDto(Playlist playlist) {
        this.setId(playlist.getId());
        this.setTitle(playlist.getTitle());
        this.setDateCreated(playlist.getDateCreated());
    }
}
