package finalproject.youtube.model.pojo;

import finalproject.youtube.model.dto.RequestPlaylistDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table( name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long   id;
    @Column(name = "title")
    private String title;
    @Column(name = "date_created")
    private LocalDateTime dateCreated;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User        owner;

    public Playlist(int id, String title, LocalDateTime dateCreated, User owner) {
        this.id = id;
        this.title = title;
        this.dateCreated = dateCreated;
        this.owner = owner;
    }

    public Playlist(RequestPlaylistDto requestPlaylist) {
        this.setTitle(requestPlaylist.getTitle());
        this.setDateCreated(LocalDateTime.now());
    }
}
