package finalproject.youtube.controller;

import finalproject.youtube.AmazonClient;
import finalproject.youtube.SessionManager;
import finalproject.youtube.Validator;
import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.exceptions.BadRequestException;
import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.dao.VideoDAO;
import finalproject.youtube.model.dto.VideoDto;
import finalproject.youtube.model.entity.Status;
import finalproject.youtube.model.entity.Uploader;
import finalproject.youtube.model.entity.User;
import finalproject.youtube.model.entity.Video;
import finalproject.youtube.model.repository.UserRepository;
import finalproject.youtube.model.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class VideoController extends BaseController {

    private static final int NUMBER_VIDEOS_PER_PAGE = 10;

    @Autowired
    VideoDAO videoDAO;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AmazonClient amazonClient;

    @Autowired
    VideoRepository videoRepository;

    private void setInitialValuesToVideo(Video video) {
        video.setStatus(Status.PENDING.toString());
        video.setVideoUrl("");
        video.setThumbnailUrl("");
        video.setNumberLikes(0);
        video.setNumberDislikes(0);
    }

    @PostMapping(value = "videos/upload")
    public ResponseEntity<VideoDto> uploadVideo(@RequestPart(value = "file") MultipartFile multipartFile,
                                                @RequestPart(value = "thumbnail") MultipartFile thumbnail,
                                                @RequestParam(value = "title") String title,
                                                @RequestParam(value = "description") String description,
                                                @RequestParam(value = "categoryId") int categoryId,

                                                HttpSession session) throws SQLException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }

        Validator.validateVideoInformation(title, categoryId);

        Video video = new Video();
        setInitialValuesToVideo(video);
        video.setTitle(title);
        video.setDescription(description);
        video.setCategoryId(categoryId);
        User owner = SessionManager.getLoggedUser(session);
        video.setOwnerId(owner.getId());
        videoDAO.uploadVideo(video);

        try {
            Thread uploader = new Uploader(video, amazonClient.convertMultiPartToFile(multipartFile),
                    amazonClient.convertMultiPartToFile(thumbnail), amazonClient, videoRepository);
            uploader.start();
        } catch (IOException e) {
            video.setStatus(Status.FAILED.toString());
        }

        return new ResponseEntity<>(video.toVideoDto(), HttpStatus.OK);
    }

    public Video validateAndGetVideo(long videoId) {
        Optional<Video> optionalVideo = videoRepository.findById(videoId);
        if (!optionalVideo.isPresent()) {
            throw new NotFoundException("Video not found!");
        }

        Video video = optionalVideo.get();
        String status = video.getStatus();
        if (status == null || !status.equals(Status.UPLOADED.toString())) {
            throw new NotFoundException("Video not found!");
        }

        return video;
    }

    @DeleteMapping(value = "videos/{id}")
    public ResponseEntity<Video> deleteVideo(@PathVariable("id") long videoId, HttpSession session) {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }

        User owner = SessionManager.getLoggedUser(session);
        Video video  = validateAndGetVideo(videoId);

        if (video.getOwnerId() != owner.getId()) {
            throw new AuthorizationException("Unauthorized");
        }
        videoRepository.deleteById(videoId);
        amazonClient.deleteFileFromS3Bucket(video.getVideoUrl());
        amazonClient.deleteFileFromS3Bucket(video.getThumbnailUrl());

        return new ResponseEntity<>(video, HttpStatus.OK);
    }


    @GetMapping(value = "videos/{id}")
    public ResponseEntity<VideoDto> getVideoById(@PathVariable("id") long videoId) {
        VideoDto video  = validateAndGetVideo(videoId).toVideoDto();

        return new ResponseEntity<>(video, HttpStatus.OK);
    }

    @GetMapping(value = "videos/title/{title}/{page}")
    public ResponseEntity<List<VideoDto>> getVideosByTitle(@PathVariable("title") String title,
                                                           @PathVariable("page") int page) {
        List<Video> videos = videoRepository.findAllByTitleContainingAndStatus(title, Status.UPLOADED.toString(),
                PageRequest.of(page, NUMBER_VIDEOS_PER_PAGE));

        if (videos.isEmpty()) {
            throw new NotFoundException("No videos found!");
        }

        List<VideoDto> videoDtos = new ArrayList<>();
        for (Video video : videos) {
            videoDtos.add(video.toVideoDto());

        }

        return new ResponseEntity<>(videoDtos, HttpStatus.OK);
    }

    @PostMapping(value = "videos/{id}/like")
    public ResponseEntity<String> likeVideo(@PathVariable("id") long videoId, HttpSession session)
            throws SQLException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }

        Video video = validateAndGetVideo(videoId);
        User currentUser = SessionManager.getLoggedUser(session);
        videoDAO.setReactionToVideo(video, currentUser.getId(), VideoDAO.LIKE);

        return new ResponseEntity<>("Successfully liked video!", HttpStatus.OK);
    }

    @PostMapping(value = "videos/{id}/dislike")
    public ResponseEntity<String> dislikeVideo(@PathVariable("id") long videoId, HttpSession session)
            throws SQLException {
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException();
        }
        Video video = validateAndGetVideo(videoId);
        User currentUser = SessionManager.getLoggedUser(session);
        videoDAO.setReactionToVideo(video, currentUser.getId(), VideoDAO.DISLIKE);

        return new ResponseEntity<>("Successfully disliked video!", HttpStatus.OK);
    }

    @GetMapping("/videos/page/{page}")
    public ResponseEntity<List<VideoDto>> getAllByDateUploadedAndNumberLikes(@PathVariable("page") int page)
            throws SQLException {

        List<Video> videos = videoDAO.getAllByDateUploadedAndNumberLikes(page);
        if (videos.isEmpty()) {
            throw new NotFoundException("Videos not found!");
        }
        List<VideoDto> videoDtos = new ArrayList<>();
        for (Video video : videos) {
            videoDtos.add(video.toVideoDto());
        }
        return new ResponseEntity<>(videoDtos, HttpStatus.OK);
    }
}
