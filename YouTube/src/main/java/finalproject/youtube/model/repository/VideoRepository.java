package finalproject.youtube.model.repository;

import finalproject.youtube.model.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {
}
