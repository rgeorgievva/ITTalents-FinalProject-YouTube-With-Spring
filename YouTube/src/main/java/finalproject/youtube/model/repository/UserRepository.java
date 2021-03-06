package finalproject.youtube.model.repository;

import finalproject.youtube.model.pojo.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User getByEmail(String email);
    List<User> findAllByUsernameContaining(String username, Pageable pageable);
}
