package finalproject.youtube.model.repository;

import finalproject.youtube.model.pojo.Video;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> getAllByOwnerIdOrderByDateUploadedDesc(long ownerId, Pageable pageable);
    void deleteById(long id);
    List<Video> findAllByTitleContainingAndStatusOrderByNumberLikesDescDateUploadedDesc(String title,
                                                                                        String status,
                                                                                        Pageable pageable);
    List<Video> findAllByStatusOrderByNumberLikesDescDateUploadedDesc(String status, Pageable pageable);

}

