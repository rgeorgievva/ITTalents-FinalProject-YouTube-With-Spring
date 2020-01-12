package finalproject.youtube.controller;

import finalproject.youtube.SessionManager;
import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.model.dto.PendingVideoDto;
import finalproject.youtube.model.dto.VideoDto;
import finalproject.youtube.model.pojo.*;
import finalproject.youtube.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.List;

@RestController
public class VideoController extends BaseController {

    private static final int NUMBER_VIDEOS_PER_PAGE = 10;

    @Autowired
    VideoService videoService;

    @PostMapping(value = "videos/upload")
    public ResponseEntity<PendingVideoDto> uploadVideo(@RequestPart(value = "file") MultipartFile multipartFile,
                                                       @RequestPart(value = "thumbnail") MultipartFile thumbnail,
                                                       @RequestParam(value = "title") String title,
                                                       @RequestParam(value = "description") String description,
                                                       @RequestParam(value = "categoryId") long categoryId,
                                                       HttpSession session) throws SQLException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }
        User user = SessionManager.getLoggedUser(session);
        PendingVideoDto video = videoService.uploadVideo(multipartFile, thumbnail, title, description, categoryId, user);

        return new ResponseEntity<>(video, HttpStatus.OK);
    }

    @DeleteMapping(value = "videos/{id}")
    public ResponseEntity<VideoDto> deleteVideo(@PathVariable("id") long videoId, HttpSession session) {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }
        User owner = SessionManager.getLoggedUser(session);
        VideoDto video = videoService.deleteVideo(videoId, owner);

        return new ResponseEntity<>(video, HttpStatus.OK);
    }


    @GetMapping(value = "videos/{id}")
    public ResponseEntity<VideoDto> getVideoById(@PathVariable("id") long videoId) {
        VideoDto video = videoService.getVideoById(videoId);

        return new ResponseEntity<>(video, HttpStatus.OK);
    }

    @GetMapping(value = "videos/title/{title}/{page}")
    public ResponseEntity<List<VideoDto>> getVideosByTitle(@PathVariable("title") String title,
                                                           @PathVariable("page") int page) {
        List<VideoDto> videos = videoService.getVideosByTitle(title, page);

        return new ResponseEntity<>(videos, HttpStatus.OK);
    }

    @PostMapping(value = "videos/{id}/like")
    public void likeVideo(@PathVariable("id") long videoId, HttpSession session)
            throws SQLException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }
        User currentUser = SessionManager.getLoggedUser(session);
        videoService.likeVideo(videoId, currentUser);
    }

    @PostMapping(value = "videos/{id}/dislike")
    public void dislikeVideo(@PathVariable("id") long videoId, HttpSession session)
            throws SQLException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }
        User currentUser = SessionManager.getLoggedUser(session);
        videoService.dislikeVideo(videoId, currentUser);
    }

    @GetMapping("/videos/page/{page}")
    public ResponseEntity<List<VideoDto>> getAllByDateUploadedAndNumberLikes(@PathVariable("page") int page) {
        List<VideoDto> videos = videoService.getAllByDateUploadedAndNumberLikes(page);

        return new ResponseEntity<>(videos, HttpStatus.OK);
    }
}
