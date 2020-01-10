package finalproject.youtube.model.repository;

import finalproject.youtube.model.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> getAllByOwnerId(long ownerId);

    void deleteById(long id);

    List<Video> findAllByTitleContainingAndStatus(String title,String status, Pageable pageable);
    List<Video> findAllByTitleContainingAndStatus(String title, String status);

}

