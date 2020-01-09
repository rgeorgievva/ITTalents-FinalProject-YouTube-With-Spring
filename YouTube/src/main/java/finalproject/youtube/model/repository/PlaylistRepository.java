package finalproject.youtube.model.repository;

import finalproject.youtube.model.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    boolean existsPlaylistById(long id);

    Playlist getPlaylistById(long id);

    boolean existsPlaylistByTitle(String s);

    List<Playlist> getAllByTitle(String s);

    boolean existsPlaylistByOwnerIdAndTitle(long ownerId, String title);

    List<Playlist> getAllByOwnerId(long ownerId);
}
