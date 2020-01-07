package finalproject.youtube.model.entity;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long   id;
    @Column(name = "title")
    private String title;
    @Column(name = "date_created")
    private LocalDateTime dateCreated;
    @Column(name = "owner_id")
    private long          ownerId;

    public Playlist(int id, String title, LocalDateTime dateCreated, int ownerId) {
        this.id = id;
        this.title = title;
        this.dateCreated = dateCreated;
        this.ownerId = ownerId;
    }

    @Override
    public String toString() {
        return "Playlist{ " +
                "title = '" + title + '\'' +
                ", dateCreated = " + dateCreated +
                ", ownerId = " + ownerId +
                '}';
    }
}
