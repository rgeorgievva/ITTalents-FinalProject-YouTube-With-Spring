package finalproject.youtube.model.repository;

import finalproject.youtube.model.pojo.Playlist;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findAllByTitleContaining(String s, Pageable pageable);

    boolean existsPlaylistByOwnerIdAndTitle(long ownerId, String title);

    List<Playlist> findAllByOwnerId(long id, Pageable pageable);
}
