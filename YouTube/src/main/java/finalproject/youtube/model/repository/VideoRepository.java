package finalproject.youtube.model.repository;

import finalproject.youtube.model.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    Video getVideoById(long id);

    boolean existsVideoById(long id);

    List<Video> getAllByOwnerId(long ownerId);

    void deleteById(long id);

    List<Video> getAllByTitle(String title);
}

