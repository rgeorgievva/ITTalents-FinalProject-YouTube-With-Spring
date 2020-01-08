package finalproject.youtube.controller;

import finalproject.youtube.AmazonClient;
import finalproject.youtube.SessionManager;
import finalproject.youtube.Validator;
import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.exceptions.BadRequestException;
import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.dao.UserDAO;
import finalproject.youtube.model.dao.VideoDAO;
import finalproject.youtube.model.dto.VideoDto;
import finalproject.youtube.model.entity.User;
import finalproject.youtube.model.entity.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class VideoController extends BaseController {

    @Autowired
    VideoDAO videoDAO;

    @Autowired
    UserDAO userDAO;

    @Autowired
    AmazonClient amazonClient;

    @PostMapping(value = "videos/upload")
    public ResponseEntity<VideoDto> uploadVideo(@RequestPart(value = "file") MultipartFile multipartFile,
                                                @RequestPart(value = "thumbnail") MultipartFile thumbnail,
                                                @RequestParam(value = "title") String title,
                                                @RequestParam(value = "description") String description,
                                                @RequestParam(value = "categoryId") int categoryId,

                                                HttpSession session) throws AuthorizationException,
            BadRequestException, SQLException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }

        Validator.validateVideoInformation(title, categoryId);

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setCategoryId(categoryId);

        User owner = SessionManager.getLoggedUser(session);
        video.setOwnerId(owner.getId());

        video.setVideoUrl(amazonClient.uploadFile(multipartFile, video, false));
        video.setThumbnailUrl(amazonClient.uploadFile(thumbnail, video, true));

        long id = videoDAO.uploadVideo(video);
        video.setId(id);

        return new ResponseEntity<>(video.toVideoDto(), HttpStatus.OK);
    }

    @DeleteMapping(value = "videos/delete/{id}")
    public ResponseEntity<String> deleteVideo(@PathVariable("id") long id, HttpSession session)
            throws AuthorizationException, NotFoundException, SQLException {

        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }

        User owner = SessionManager.getLoggedUser(session);
        Video video = videoDAO.getById(id);

        if (video.getOwnerId() != owner.getId()) {
            throw new AuthorizationException("Unauthorized");
        }

        videoDAO.removeVideo(id);

        amazonClient.deleteFileFromS3Bucket(video.getVideoUrl());
        amazonClient.deleteFileFromS3Bucket(video.getThumbnailUrl());
        
        return new ResponseEntity<>("Successfully deleted video with id " + id + "!", HttpStatus.OK);
    }


    @GetMapping(value = "videos/{id}")
    public ResponseEntity<String> getVideoUrl(@PathVariable("id") long id) throws NotFoundException, SQLException {
        Video video = videoDAO.getById(id);

        String url = video.getVideoUrl();

        return new ResponseEntity<>(url, HttpStatus.OK);
    }

    @GetMapping(value = "videos/get/{id}")
    public ResponseEntity<VideoDto> getVideoById(@PathVariable("id") long id) throws NotFoundException, SQLException {
         VideoDto video = videoDAO.getById(id).toVideoDto();

         return new ResponseEntity<>(video, HttpStatus.OK);
    }

    @GetMapping(value = "videos/get/title/{title}")
    public ResponseEntity<List<VideoDto>> getVideosByTitle(@PathVariable("title") String title)
            throws NotFoundException, SQLException {
        List<VideoDto> videos =  new ArrayList<>();
        for (Video video : videoDAO.getAllByTitle(title)) {
            videos.add(video.toVideoDto());
        }

        return new ResponseEntity<>(videos, HttpStatus.OK);
    }

    @PostMapping(value = "videos/like/{id}")
    public ResponseEntity<String> likeVideo(@PathVariable("id") int videoId, HttpSession session)
            throws AuthorizationException, NotFoundException, SQLException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }

        User currentUser = SessionManager.getLoggedUser(session);
        Video video = videoDAO.getById(videoId);

        videoDAO.likeVideo(video, currentUser);

        return new ResponseEntity<>("Successfully liked video!", HttpStatus.OK);
    }

    @PostMapping(value = "videos/dislike/{id}")
    public ResponseEntity<String> dislikeVideo(@PathVariable("id") int videoId, HttpSession session) throws AuthorizationException, NotFoundException, SQLException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }

        User currentUser = SessionManager.getLoggedUser(session);
        Video video = videoDAO.getById(videoId);

        videoDAO.dislikeVideo(video, currentUser);

        return new ResponseEntity<>("Successfully disliked video!", HttpStatus.OK);
    }

    @GetMapping(value = "videos/by/user/{userId}")
    public ResponseEntity<List<VideoDto>> getVideosUploadedByUser(@PathVariable("userId") int userId)
            throws NotFoundException, SQLException {
        User user = userDAO.getById(userId);
        List<VideoDto> videos = new ArrayList<>();

        for (Video video : videoDAO.getAllVideosByOwner(user)) {
            videos.add(video.toVideoDto());
        }

        return new ResponseEntity<>(videos, HttpStatus.OK);
    }
}
