package finalproject.youtube.controller;

import finalproject.youtube.SessionManager;
import finalproject.youtube.exceptions.UserException;
import finalproject.youtube.model.dao.UserDAO;
import finalproject.youtube.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
public class UserController extends BaseController {

    @Autowired
    UserDAO userDAO;

    @PostMapping(value = "users/register")
    public void register(@RequestBody User user) throws UserException {
        userDAO.registerUser(user);
    }

    @PostMapping(value = "users/login")
    public void login(HttpSession session, @RequestBody User user) throws UserException {
        int userId = userDAO.loginUser(user.getEmail(), user.getPassword());
        user.setId(userId);
        SessionManager.logUser(session, user);
    }

    @PostMapping(value = "users/logout")
    public void logout(HttpSession session) throws UserException {
        if (!SessionManager.validateLogged(session)) {
            throw new UserException("Not logged in!");
        }

        session.invalidate();
    }

    @PutMapping(value = "users/profile/edit")
    public void editUserProfile(HttpSession session, @RequestBody User user) throws UserException {
        if (!SessionManager.validateLogged(session)) {
            throw new UserException("Unauthorized");
        }
        int loggedUserId = SessionManager.getLoggedUser(session).getId();
        user.setId(loggedUserId);
        userDAO.editProfile(user);
    }

    @GetMapping(value = "users/get/by/{username}")
    public List<User> getByUsername(@PathVariable("username") String username) throws UserException {
        return userDAO.findByUsername(username);
    }

    @PostMapping(value = "users/subscribe/to/user")
    public void subscribeToUser(@RequestBody User subscribedTo, HttpSession session)
            throws UserException {
        if (!SessionManager.validateLogged(session)) {
            throw new UserException("Unauthorized");
        }

        User subscriber = SessionManager.getLoggedUser(session);
        userDAO.subscribeToUser(subscriber, subscribedTo);
    }

    @DeleteMapping(value = "users/unsubscribe/from/user")
    public void unsubscribeFromUser(@RequestBody User unsubscribedFrom, HttpSession session) throws UserException {
        if (!SessionManager.validateLogged(session)) {
            throw new UserException("Unauthorized");
        }

        User subscriber = SessionManager.getLoggedUser(session);
        userDAO.unsubscribeFromUser(subscriber, unsubscribedFrom);
    }

}
