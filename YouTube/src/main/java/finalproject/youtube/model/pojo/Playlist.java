package finalproject.youtube.model.pojo;

import finalproject.youtube.model.dto.SmallPlaylistDto;
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

    public Playlist(String title) {
        this.setTitle(title);
        this.setDateCreated(LocalDateTime.now());
    }

    public SmallPlaylistDto toSmallDto() {
        return new SmallPlaylistDto(id, title, owner.toSmallUserDto(), dateCreated);
    }
}
