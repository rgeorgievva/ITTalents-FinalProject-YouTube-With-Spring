package finalproject.youtube.controller;

import finalproject.youtube.AmazonClient;
import finalproject.youtube.SessionManager;
import finalproject.youtube.exceptions.UserException;
import finalproject.youtube.exceptions.VideoException;
import finalproject.youtube.model.dao.UserDAO;
import finalproject.youtube.model.dao.VideoDAO;
import finalproject.youtube.model.entity.User;
import finalproject.youtube.model.entity.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
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
    public void uploadVideo(@RequestPart(value = "file") MultipartFile multipartFile,
                            @RequestParam(value = "title") String title,
                            @RequestParam(value = "description") String description,
                            @RequestParam(value = "categoryId") int categoryId,
                            HttpSession session) throws VideoException, UserException {
        if (!SessionManager.validateLogged(session)) {
            throw new UserException("Unauthorized!");
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
    }

    @DeleteMapping(value = "videos/delete/{id}")
    public void deleteVideo(@PathVariable("id") int id, HttpSession session) throws UserException, VideoException {
         if (!SessionManager.validateLogged(session)) {
             throw new UserException("Unauthorized");
         }

         User owner = SessionManager.getLoggedUser(session);
         Video video = videoDAO.getById(id);
         video.setOwnerId(owner.getId());

         amazonClient.deleteFileFromS3Bucket(video.getVideoUrl());
         videoDAO.removeVideo(video);
    }

    @GetMapping(value = "videos/get/{id}")
    public Video getVideoById(@PathVariable("id") int id) throws VideoException {
         return videoDAO.getById(id);
    }

    @GetMapping(value = "videos/get/title/{title}")
    public List<Video> getVideosByTitle(@PathVariable("title") String title) throws VideoException {
        return videoDAO.getAllByTitle(title);
    }

    @PostMapping(value = "videos/like/{id}")
    public void likeVideo(@PathVariable("id") int videoId, HttpSession session) throws UserException, VideoException {
        if (!SessionManager.validateLogged(session)) {
            throw new UserException("Unauthorized");
        }

        User currentUser = SessionManager.getLoggedUser(session);
        Video video = videoDAO.getById(videoId);

        videoDAO.likeVideo(video, currentUser);
    }

    @PostMapping(value = "videos/dislike/{id}")
    public void dislikeVideo(@PathVariable("id") int videoId, HttpSession session) throws UserException, VideoException {
        if (!SessionManager.validateLogged(session)) {
            throw new UserException("Unauthorized");
        }

        User currentUser = SessionManager.getLoggedUser(session);
        Video video = videoDAO.getById(videoId);

        videoDAO.dislikeVideo(video, currentUser);
    }

    @GetMapping(value = "videos/by/user/{userId}")
    public List<Video> getVideosUploadedByUser(@PathVariable("userId") int userId) throws UserException, VideoException {
        User user = userDAO.getById(userId);

        return videoDAO.getAllVideosByOwner(user);
    }
}
