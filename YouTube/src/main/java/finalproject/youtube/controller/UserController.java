package finalproject.youtube.controller;

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

    public static boolean validateLogged(HttpSession session, User user) {
        if (session.isNew()) {
            return false;
        }

        if (session.getAttribute("user") == null) {
            return false;
        }

        User u = (User) session.getAttribute("user");
        if (!u.getEmail().equals(user.getEmail())) {
            return false;
        }

        return true;
    }

    @PostMapping(value = "users/register")
    public void register(@RequestBody User user) throws UserException {
        userDAO.registerUser(user);
    }

    @PostMapping(value = "users/login")
    public void login(HttpSession session, @RequestBody User user) throws UserException {
        userDAO.loginUser(user.getEmail(), user.getPassword());
        session.setAttribute("user", user);
    }

    @PutMapping(value = "users/profile/edit")
    public void editUserProfile(HttpSession session, @RequestBody User user) throws UserException {
        if (!validateLogged(session, user)) {
            throw new UserException("Unauthorized");
        }
        userDAO.editProfile(user);
    }

    @GetMapping(value = "users/get/by/{username}")
    public List<User> getByUsername(@PathVariable("username") String username) throws UserException {
        return userDAO.findByUsername(username);
    }

    @PostMapping(value = "users/subscribe/to/user")
    public void subscribeToUser(@RequestBody List<User> users, HttpSession session)
            throws UserException {
        if (users.size() < 2) {
            throw new UserException("Missing subscriber");
        }
        User subscriber = users.get(0);
        User subscribedTo = users.get(1);
        if (!validateLogged(session, subscriber)) {
            throw new UserException("Unauthorized");
        }

        userDAO.subscribeToUser(subscriber, subscribedTo);
    }

    @DeleteMapping(value = "users/unsubscribe/from/user")
    public void unsubscribeFromUser(@RequestBody List<User> users, HttpSession session)
            throws UserException {
        if (users.size() < 2) {
            throw new UserException("Missing subscriber");
        }
        User subscriber = users.get(0);
        User subscribedTo = users.get(1);
        if (!validateLogged(session, subscriber)) {
            throw new UserException("Unauthorized");
        }

        userDAO.unsubscribeFromUser(subscriber, subscribedTo);
    }

}
