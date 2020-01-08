package finalproject.youtube.model.repository;

import finalproject.youtube.model.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    public Video getVideoById(long id);

    public boolean existsVideoById(long id);
}
