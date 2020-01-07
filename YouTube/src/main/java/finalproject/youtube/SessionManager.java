package finalproject.youtube;

import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.model.entity.User;

import javax.servlet.http.HttpSession;

public class SessionManager {

    private static final String LOGGED = "logged";

    public static boolean validateLogged(HttpSession session) {
        if (session.isNew()) {
            return false;
        }

        if (session.getAttribute(LOGGED) == null) {
            return false;
        }

        return true;
    }

    public static User getLoggedUser(HttpSession session) throws AuthorizationException {
        if (validateLogged(session)) {
            return (User) session.getAttribute("user");
        }

        throw new AuthorizationException("Not logged!");
    }

    public static void logUser(HttpSession session, User user) {
        session.setAttribute(LOGGED, true);
        session.setAttribute("user", user);
    }
}
