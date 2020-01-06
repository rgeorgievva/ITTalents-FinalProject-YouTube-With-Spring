package finalproject.youtube.controller;

import finalproject.youtube.SessionManager;
import finalproject.youtube.exceptions.UserException;
import finalproject.youtube.model.dao.UserDAO;
import finalproject.youtube.model.dto.LoginUserDto;
import finalproject.youtube.model.dto.NoPasswordUserDto;
import finalproject.youtube.model.dto.RegisterUserDto;
import finalproject.youtube.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
public class UserController extends BaseController {

    @Autowired
    UserDAO userDAO;

    @PostMapping(value = "users/register")
    public ResponseEntity<NoPasswordUserDto> register(@RequestBody RegisterUserDto registerUser) throws UserException {
        if (!registerUser.getPassword().equals(registerUser.getConfirmPassword())) {
            System.out.println(registerUser.getPassword());
            System.out.println(registerUser.getConfirmPassword());
            throw new UserException("Confirmed password doesn't match password!");
        }

        User user = User.registerDtoToUser(registerUser);
        user.setId(userDAO.registerUser(user));

        return new ResponseEntity<>(user.toNoPasswordUserDto(), HttpStatus.CREATED);
    }

    @PostMapping(value = "users/login")
    public ResponseEntity<NoPasswordUserDto> login(HttpSession session, @RequestBody LoginUserDto loginUser) throws UserException {
        int userId = userDAO.loginUser(loginUser.getEmail(), loginUser.getPassword());

        User user = userDAO.getById(userId);
        SessionManager.logUser(session, user);

        return new ResponseEntity<>(user.toNoPasswordUserDto(), HttpStatus.OK);
    }

    @PostMapping(value = "users/logout")
    public ResponseEntity<String> logout(HttpSession session) throws UserException {
        if (!SessionManager.validateLogged(session)) {
            throw new UserException("Not logged in!");
        }

        session.invalidate();

        return new ResponseEntity<>("Logged out successfully!", HttpStatus.OK);
    }

    @PutMapping(value = "users/profile/edit")
    public ResponseEntity<User> editUserProfile(HttpSession session, @RequestBody User user) throws UserException {
        if (!SessionManager.validateLogged(session)) {
            throw new UserException("Unauthorized");
        }
        int loggedUserId = SessionManager.getLoggedUser(session).getId();
        user.setId(loggedUserId);
        userDAO.editProfile(user);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping(value = "users/{username}")
    public List<NoPasswordUserDto> getByUsername(@PathVariable("username") String username) throws UserException {
        List<NoPasswordUserDto> users = userDAO.findByUsername(username);
        if (users.isEmpty()) {
            throw new UserException("No users with username " + username + " found!");
        }

        return users;
    }

    @PostMapping(value = "users/subscribe/{id}")
        public ResponseEntity<String> subscribeToUser(@PathVariable("id") int subscribedToId, HttpSession session)
            throws UserException {
        if (!SessionManager.validateLogged(session)) {
            throw new UserException("Unauthorized");
        }

        User subscriber = SessionManager.getLoggedUser(session);
        userDAO.subscribeToUser(subscriber, subscribedToId);

        return new ResponseEntity<>("Subscribed successfully!", HttpStatus.OK);
    }

    @DeleteMapping(value = "users/unsubscribe/{id}")
    public ResponseEntity<String> unsubscribeFromUser(@PathVariable("id") int unsubscribeFromId, HttpSession session) throws UserException {
        if (!SessionManager.validateLogged(session)) {
            throw new UserException("Unauthorized");
        }

        User subscriber = SessionManager.getLoggedUser(session);
        userDAO.unsubscribeFromUser(subscriber, unsubscribeFromId);

        return new ResponseEntity<>("Unsubscribed successfully!", HttpStatus.OK);
    }

}
