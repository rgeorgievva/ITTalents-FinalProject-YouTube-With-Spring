package finalproject.youtube.controller;

import finalproject.youtube.SessionManager;
import finalproject.youtube.Validator;
import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.exceptions.BadRequestException;
import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.dao.UserDAO;
import finalproject.youtube.model.dto.LoginUserDto;
import finalproject.youtube.model.dto.NoPasswordUserDto;
import finalproject.youtube.model.dto.RegisterUserDto;
import finalproject.youtube.model.entity.User;
import finalproject.youtube.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.List;

@RestController
public class UserController extends BaseController {

    @Autowired
    UserDAO userDAO;

    @Autowired
    UserRepository userRepository;

    @PostMapping(value = "users/register")
    public ResponseEntity<NoPasswordUserDto> register(@RequestBody RegisterUserDto registerUser) throws BadRequestException, SQLException {

        Validator.validateRegisterDto(registerUser);

        User user = User.registerDtoToUser(registerUser);
        String encodedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        System.out.println("Encoded password: " + encodedPassword);
        user.setPassword(encodedPassword);
        user.setId(userDAO.registerUser(user));

        return new ResponseEntity<>(user.toNoPasswordUserDto(), HttpStatus.CREATED);
    }

    @PostMapping(value = "users/login")
    public ResponseEntity<NoPasswordUserDto> login(HttpSession session, @RequestBody LoginUserDto loginUser)
            throws BadRequestException {

        User user = userRepository.getByEmail(loginUser.getEmail());
        if (user == null) {
            System.out.println("User is null");
            throw new BadRequestException("Invalid email or password!");
        }

        if (!BCrypt.checkpw(loginUser.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password!");
        }

        SessionManager.logUser(session, user);

        return new ResponseEntity<>(user.toNoPasswordUserDto(), HttpStatus.OK);
    }

    @PostMapping(value = "users/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();

        return new ResponseEntity<>("Logged out successfully!", HttpStatus.OK);
    }

    @PutMapping(value = "users/profile/edit")
    public ResponseEntity<User> editUserProfile(HttpSession session, @RequestBody User user)
            throws AuthorizationException, SQLException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }
        long loggedUserId = SessionManager.getLoggedUser(session).getId();
        user.setId(loggedUserId);
        userDAO.editProfile(user);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping(value = "users/{username}")
    public List<NoPasswordUserDto> getByUsername(@PathVariable("username") String username) throws NotFoundException, SQLException {
        List<NoPasswordUserDto> users = userDAO.findByUsername(username);
        if (users.isEmpty()) {
            throw new NotFoundException("No users with username " + username + " found!");
        }

        return users;
    }

    @PostMapping(value = "users/subscribe/{id}")
        public ResponseEntity<String> subscribeToUser(@PathVariable("id") int subscribedToId, HttpSession session)
            throws AuthorizationException, SQLException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }

        User subscriber = SessionManager.getLoggedUser(session);
        userDAO.subscribeToUser(subscriber, subscribedToId);

        return new ResponseEntity<>("Subscribed successfully!", HttpStatus.OK);
    }

    @DeleteMapping(value = "users/unsubscribe/{id}")
    public ResponseEntity<String> unsubscribeFromUser(@PathVariable("id") int unsubscribeFromId, HttpSession session)
            throws AuthorizationException, SQLException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }

        User subscriber = SessionManager.getLoggedUser(session);
        userDAO.unsubscribeFromUser(subscriber, unsubscribeFromId);

        return new ResponseEntity<>("Unsubscribed successfully!", HttpStatus.OK);
    }

}
