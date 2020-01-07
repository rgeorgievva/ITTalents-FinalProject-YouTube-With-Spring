package finalproject.youtube.controller;

import finalproject.youtube.AmazonClient;
import finalproject.youtube.SessionManager;
import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.exceptions.VideoException;
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
                                                @RequestParam(value = "title") String title,
                                                @RequestParam(value = "description") String description,
                                                @RequestParam(value = "categoryId") int categoryId,
                                                HttpSession session) throws VideoException, AuthorizationException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }
        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setCategoryId(categoryId);
        video.setThumbnailUrl("");

        User owner = SessionManager.getLoggedUser(session);
        video.setOwnerId(owner.getId());

        video.setVideoUrl(amazonClient.uploadFile(multipartFile, video));

        videoDAO.uploadVideo(video);

        return new ResponseEntity<>(video.toVideoDto(), HttpStatus.OK);
    }

    @DeleteMapping(value = "videos/delete/{id}")
    public ResponseEntity<String> deleteVideo(@PathVariable("id") int id, HttpSession session) throws VideoException, AuthorizationException {
         if (!SessionManager.validateLogged(session)) {
             throw new AuthorizationException();
         }

         User owner = SessionManager.getLoggedUser(session);
         Video video = videoDAO.getById(id);
         video.setOwnerId(owner.getId());

         amazonClient.deleteFileFromS3Bucket(video.getVideoUrl());
         videoDAO.removeVideo(video);

         return new ResponseEntity<>("Successfully deleted video with id " + id + "!", HttpStatus.OK);
    }

    @GetMapping(value = "videos/get/{id}")
    public ResponseEntity<VideoDto> getVideoById(@PathVariable("id") int id) throws VideoException {
         VideoDto video = videoDAO.getById(id).toVideoDto();

         return new ResponseEntity<>(video, HttpStatus.OK);
    }

    @GetMapping(value = "videos/get/title/{title}")
    public ResponseEntity<List<VideoDto>> getVideosByTitle(@PathVariable("title") String title) throws VideoException {
        List<VideoDto> videos =  new ArrayList<>();
        for (Video video : videoDAO.getAllByTitle(title)) {
            videos.add(video.toVideoDto());
        }

        return new ResponseEntity<>(videos, HttpStatus.OK);
    }

    @PostMapping(value = "videos/like/{id}")
    public ResponseEntity<String> likeVideo(@PathVariable("id") int videoId, HttpSession session) throws VideoException, AuthorizationException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }

        User currentUser = SessionManager.getLoggedUser(session);
        Video video = videoDAO.getById(videoId);

        videoDAO.likeVideo(video, currentUser);

        return new ResponseEntity<>("Successfully liked video!", HttpStatus.OK);
    }

    @PostMapping(value = "videos/dislike/{id}")
    public ResponseEntity<String> dislikeVideo(@PathVariable("id") int videoId, HttpSession session) throws VideoException, AuthorizationException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }

        User currentUser = SessionManager.getLoggedUser(session);
        Video video = videoDAO.getById(videoId);

        videoDAO.dislikeVideo(video, currentUser);

        return new ResponseEntity<>("Successfully disliked video!", HttpStatus.OK);
    }

    @GetMapping(value = "videos/by/user/{userId}")
    public ResponseEntity<List<VideoDto>> getVideosUploadedByUser(@PathVariable("userId") int userId) throws
            VideoException, NotFoundException, SQLException {
        User user = userDAO.getById(userId);
        List<VideoDto> videos = new ArrayList<>();

        for (Video video : videoDAO.getAllVideosByOwner(user)) {
            videos.add(video.toVideoDto());
        }

        return new ResponseEntity<>(videos, HttpStatus.OK);
    }
}
