package finalproject.youtube.controller;

import finalproject.youtube.exceptions.UserException;
import finalproject.youtube.model.dao.UserDAO;
import finalproject.youtube.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

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


}
