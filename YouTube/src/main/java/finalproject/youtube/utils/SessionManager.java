package finalproject.youtube.utils;

import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.model.pojo.User;

import javax.servlet.http.HttpSession;

public class SessionManager {

    private static final String LOGGED = "logged";

    public static void validateLogged(HttpSession session) {
        if (session.isNew()) {
            throw new AuthorizationException();
        }
        if (session.getAttribute(LOGGED) == null) {
            throw new AuthorizationException();
        }
    }

    public static User getLoggedUser(HttpSession session) throws AuthorizationException {
        validateLogged(session);
        User user = (User) session.getAttribute("user");
        return user;
    }

    public static void logUser(HttpSession session, User user) {
        session.setAttribute(LOGGED, true);
        session.setAttribute("user", user);
    }
}
