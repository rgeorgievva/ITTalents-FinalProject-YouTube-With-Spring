package finalproject.youtube.controller;

import finalproject.youtube.SessionManager;
import finalproject.youtube.Validator;
import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.exceptions.BadRequestException;
import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.dao.UserDAO;
import finalproject.youtube.model.dto.*;
import finalproject.youtube.model.entity.User;
import finalproject.youtube.model.entity.Video;
import finalproject.youtube.model.repository.UserRepository;
import finalproject.youtube.model.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class UserController extends BaseController {

    @Autowired
    UserDAO userDAO;

    @Autowired
    UserRepository userRepository;

    @Autowired
    VideoRepository videoRepository;

    //check if the given email is unique
    private boolean isDuplicateEmail(String email) {
        User user = userRepository.getByEmail(email);
        if (user != null) {
            return true;
        }

        return false;
    }

    @PostMapping(value = "users/register")
    public ResponseEntity<NoPasswordUserDto> register(@RequestBody RegisterUserDto registerUser) throws BadRequestException, SQLException {

        Validator.validateRegisterDto(registerUser);

        if (isDuplicateEmail(registerUser.getEmail())) {
            throw new BadRequestException("There is already an account with this email.");
        }

        User user = User.registerDtoToUser(registerUser);
        String encodedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

        user.setPassword(encodedPassword);
        user.setId(userDAO.registerUser(user));

        return new ResponseEntity<>(user.toNoPasswordUserDto(), HttpStatus.CREATED);
    }

    @PostMapping(value = "users/login")
    public ResponseEntity<NoPasswordUserDto> login(HttpSession session, @RequestBody LoginUserDto loginUser)
            throws BadRequestException {

        User user = userRepository.getByEmail(loginUser.getEmail());
        if (user == null || loginUser.getPassword() == null) {
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

    @PutMapping(value = "/users/password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDto passwordDto, HttpSession session)
            throws AuthorizationException, BadRequestException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }

        User user = SessionManager.getLoggedUser(session);
        Validator.validateChangePasswordInformation(passwordDto, user);
        user.setPassword(BCrypt.hashpw(passwordDto.getNewPassword(), BCrypt.gensalt()));
        userRepository.save(user);

        return new ResponseEntity<>("Password changed successfully!", HttpStatus.OK);
    }

    @GetMapping(value = "users/{username}")
    public ResponseEntity<List<NoPasswordUserDto>> getByUsername(@PathVariable("username") String username) throws NotFoundException, SQLException {
        List<User> users = userRepository.getAllByUsername(username);
        if (users.isEmpty()) {
            throw new NotFoundException("No users with username " + username + " found!");
        }

        List<NoPasswordUserDto> usersWithoutPass = new ArrayList<>();
        for (User user : users) {
            usersWithoutPass.add(user.toNoPasswordUserDto());
        }

        return new ResponseEntity<>(usersWithoutPass, HttpStatus.OK);
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


    @GetMapping(value = "users/{userId}/videos")
    public ResponseEntity<List<VideoDto>> getVideosUploadedByUser(@PathVariable("userId") long userId)
            throws NotFoundException {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (!optionalUser.isPresent()) {
            throw new NotFoundException("User not found!");
        }

        List<VideoDto> videos = new ArrayList<>();

        for (Video video : videoRepository.getAllByOwnerId(userId)) {
            videos.add(video.toVideoDto());
        }

        if (videos.isEmpty()) {
            throw new NotFoundException("User has no videos!");
        }

        return new ResponseEntity<>(videos, HttpStatus.OK);
    }

}
