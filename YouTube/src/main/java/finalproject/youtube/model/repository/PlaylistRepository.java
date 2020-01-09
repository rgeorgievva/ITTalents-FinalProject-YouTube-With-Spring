package finalproject.youtube.model.repository;

import finalproject.youtube.model.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    Optional<List<Playlist>> findAllByTitleContaining(String s);

    boolean existsPlaylistByOwnerIdAndTitle(long ownerId, String title);

}
