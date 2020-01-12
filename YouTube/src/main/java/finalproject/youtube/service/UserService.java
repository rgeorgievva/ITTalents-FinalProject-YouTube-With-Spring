package finalproject.youtube.service;

import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.utils.Validator;
import finalproject.youtube.exceptions.BadRequestException;
import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.dao.UserDAO;
import finalproject.youtube.model.dto.*;
import finalproject.youtube.model.pojo.Playlist;
import finalproject.youtube.model.pojo.User;
import finalproject.youtube.model.pojo.Video;
import finalproject.youtube.utils.mail.ConfirmRegistration;
import finalproject.youtube.model.repository.PlaylistRepository;
import finalproject.youtube.model.repository.UserRepository;
import finalproject.youtube.model.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserDAO userDAO;

    @Autowired
    UserRepository userRepository;

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    PlaylistRepository playlistRepository;

    private static final int NUMBER_ITEMS_PER_PAGE = 10;

    public NoPasswordUserDto createUser(RegisterUserDto registerUser) throws SQLException {
        Validator.validateRegisterDto(registerUser);
        if (userRepository.getByEmail(registerUser.getEmail()) != null) {
            throw new BadRequestException("There is already an account with this email.");
        }
        User user = User.registerDtoToUser(registerUser);
        userDAO.registerUser(user);
        Thread verificator = new ConfirmRegistration(user, userRepository);
        verificator.start();
        return user.toNoPasswordUserDto();
    }

    public User login(LoginUserDto loginUser) {
        User user = userRepository.getByEmail(loginUser.getEmail());
        if (user == null || loginUser.getPassword() == null) {
            throw new BadRequestException("Invalid email or password!");
        }
        if (!BCrypt.checkpw(loginUser.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password!");
        }

        return user;
    }

    public NoPasswordUserDto changePassword(ChangePasswordDto passwordDto, User loggedUser) {
        Validator.validateChangePasswordInformation(passwordDto, loggedUser);
        loggedUser.setPassword(passwordDto.getNewPassword());

        return userRepository.save(loggedUser).toNoPasswordUserDto();
    }

    public NoPasswordUserDto editProfile(EditProfileDto profileDto, User loggedUser) {
        Validator.validateEditProfileInformation(profileDto, loggedUser);
        String firstName = profileDto.getFirstName();
        String lastName = profileDto.getLastName();
        //if first or last name is null -> the user doesn't want to change it
        if (firstName != null) {
            loggedUser.setFirstName(firstName);
        }
        if (lastName != null) {
            loggedUser.setLastName(lastName);
        }

        return userRepository.save(loggedUser).toNoPasswordUserDto();
    }

    public List<NoPasswordUserDto> getByUsername(String username, int page) {
        List<User> users = userRepository.findAllByUsernameContaining(username,
                PageRequest.of(page, NUMBER_ITEMS_PER_PAGE));
        if (users.isEmpty()) {
            throw new NotFoundException("No users found!");
        }

        List<NoPasswordUserDto> usersWithoutPass = new ArrayList<>();
        for (User user : users) {
            usersWithoutPass.add(user.toNoPasswordUserDto());
        }

        return usersWithoutPass;
    }

    private void validateUser(long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new NotFoundException("User not found");
        }
    }

    public void subscribeToUser(long subscribedToId, User loggedUser) throws SQLException {
        if (loggedUser.getId() == subscribedToId) {
            throw new BadRequestException("You cannot subscribe/unsubscribe to yourself!");
        }
        validateUser(subscribedToId);
        if (userDAO.hasSubscribedTo(loggedUser.getId(), subscribedToId)) {
            throw new BadRequestException("You have already subscribed to this user!");
        }
        userDAO.subscribeToUser(loggedUser, subscribedToId);
    }

    public void unsubscribeFromUser (long unsubscribeFromId, User loggedUser) throws SQLException {
        if (loggedUser.getId() == unsubscribeFromId) {
            throw new BadRequestException("You cannot subscribe/unsubscribe to yourself!");
        }
        validateUser(unsubscribeFromId);
        if (!userDAO.hasSubscribedTo(loggedUser.getId(), unsubscribeFromId)) {
            throw new BadRequestException("You are not subscribed to this user!");
        }
        userDAO.unsubscribeFromUser(loggedUser, unsubscribeFromId);
    }


    public List<VideoDto> getVideosUploadedByUser(long userId, int page) {
        validateUser(userId);
        List<VideoDto> videos = new ArrayList<>();
        for (Video video : videoRepository.getAllByOwnerIdOrderByDateUploadedDesc(userId,
                PageRequest.of(page, NUMBER_ITEMS_PER_PAGE))) {
            videos.add(video.toVideoDto());
        }
        if (videos.isEmpty()) {
            throw new NotFoundException("User has no videos!");
        }

        return videos;
    }

    public List<ResponsePlaylistDto> getPlaylistsByUser(int page, long ownerId) {
        validateUser(ownerId);
        List<Playlist> playlists = playlistRepository.findAllByOwnerId(ownerId,
                PageRequest.of(page, NUMBER_ITEMS_PER_PAGE));
        if (playlists.isEmpty()) {
            throw new NotFoundException("No playlists found!");
        }
        List<ResponsePlaylistDto> playlistDtos = new ArrayList<>();
        for (Playlist playlist : playlists) {
            playlistDtos.add(new ResponsePlaylistDto(playlist));
        }
        return playlistDtos;
    }

    public User verifyAccount(long userId, String verificationURL) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent() || !optionalUser.get().getVerificationURL().equals(verificationURL)){
            throw new BadRequestException("The verification link used is not valid!");
        }
        User user = optionalUser.get();
        if(user.getStatus().equals(User.UserStatus.VERIFIED.toString())){
            throw new AuthorizationException("This account has already been verified!");
        }
        user.setStatus(User.UserStatus.VERIFIED.toString());
        userRepository.save(user);
        return user;
    }
}
