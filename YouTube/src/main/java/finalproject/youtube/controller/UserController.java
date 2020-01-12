package finalproject.youtube.controller;

import finalproject.youtube.utils.SessionManager;
import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.model.dto.*;
import finalproject.youtube.model.pojo.User;
import finalproject.youtube.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.List;

@RestController
public class UserController extends BaseController {

    @Autowired
    UserService userService;


    @PostMapping(value = "users/register")
    public ResponseEntity<NoPasswordUserDto> register(@RequestBody RegisterUserDto registerUser) throws SQLException {
        NoPasswordUserDto user = userService.createUser(registerUser);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping(value = "users/login")
    public ResponseEntity<NoPasswordUserDto> login(HttpSession session, @RequestBody LoginUserDto loginUser) {
        User user = userService.login(loginUser);
        if (!user.getStatus().equals(User.UserStatus.VERIFIED.toString())) {
            throw new AuthorizationException("Account not verified!");
        }
        SessionManager.logUser(session, user);
        return new ResponseEntity<>(user.toNoPasswordUserDto(), HttpStatus.OK);
    }

    @GetMapping(value = "/users/verify/{user_id}/{verification_url}")
    public ResponseEntity<String> verifyAccount(@PathVariable ("verification_url") String verificationURL,
                                                @PathVariable ("user_id") long userId,
                                                HttpSession session){
        User verifiedUser = userService.verifyAccount(userId, verificationURL);
        SessionManager.logUser(session, verifiedUser);
        return new ResponseEntity <>("Account verified", HttpStatus.OK);
    }

    @PostMapping(value = "users/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();

        return new ResponseEntity<>("Logged out successfully!", HttpStatus.OK);
    }

    @PutMapping(value = "/users/password")
    public ResponseEntity<NoPasswordUserDto> changePassword(@RequestBody ChangePasswordDto passwordDto, HttpSession session) {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }
        User user = SessionManager.getLoggedUser(session);
        NoPasswordUserDto userDto = userService.changePassword(passwordDto, user);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @PutMapping(value = "/users")
    public ResponseEntity<NoPasswordUserDto> editProfile(@RequestBody EditProfileDto profileDto, HttpSession session) {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }
        User user = SessionManager.getLoggedUser(session);
        NoPasswordUserDto userDto = userService.editProfile(profileDto, user);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @GetMapping(value = "users/{username}/{page}")
    public ResponseEntity<List<NoPasswordUserDto>> getByUsername(@PathVariable("username") String username,
                                                                 @PathVariable("page") int page) {
        List<NoPasswordUserDto> users = userService.getByUsername(username, page);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping(value = "users/subscribe/{id}")
        public ResponseEntity<String> subscribeToUser(@PathVariable("id") long subscribedToId, HttpSession session) throws SQLException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }
        User subscriber = SessionManager.getLoggedUser(session);
        userService.subscribeToUser(subscribedToId, subscriber);
        return new ResponseEntity<>("Subscribed successfully!", HttpStatus.OK);
    }

    @DeleteMapping(value = "users/unsubscribe/{id}")
    public ResponseEntity<String> unsubscribeFromUser(@PathVariable("id") long unsubscribeFromId, HttpSession session)
            throws SQLException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }
        User subscriber = SessionManager.getLoggedUser(session);
        userService.unsubscribeFromUser(unsubscribeFromId, subscriber);
        return new ResponseEntity<>("Unsubscribed successfully!", HttpStatus.OK);
    }

    @GetMapping(value = "users/{userId}/videos/{page}")
    public ResponseEntity<List<VideoDto>> getVideosUploadedByUser(@PathVariable("userId") long userId,
                                                                  @PathVariable("page") int page) {
        List<VideoDto> videos = userService.getVideosUploadedByUser(userId, page);
        return new ResponseEntity<>(videos, HttpStatus.OK);
    }

    @GetMapping(value = "users/{userId}/playlists/{page}")
    public ResponseEntity<List<ResponsePlaylistDto>> getPlaylistsOfUser(@PathVariable("userId") long userId,
                                                                        @PathVariable("page") int page) {
        List<ResponsePlaylistDto> playlists = userService.getPlaylistsByUser(page, userId);
        return new ResponseEntity<>(playlists, HttpStatus.OK);
    }

}
