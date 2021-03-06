package finalproject.youtube.model.repository;

import finalproject.youtube.model.pojo.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<List<Comment>> findAllByRepliedToId(long repliedToId);

    Optional<List<Comment>> findAllByVideoIdAndRepliedToIsNullOrderByTimePostedDesc(long videoId);
}
