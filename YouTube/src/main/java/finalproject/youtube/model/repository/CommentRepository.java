package finalproject.youtube.model.repository;

import finalproject.youtube.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    public Comment getCommentById(long id);

    public boolean existsCommentById(long id);
}
