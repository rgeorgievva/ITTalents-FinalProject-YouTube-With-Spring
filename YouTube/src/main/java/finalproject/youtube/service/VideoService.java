package finalproject.youtube.service;

import finalproject.youtube.utils.AmazonClient;
import finalproject.youtube.utils.Uploader;
import finalproject.youtube.utils.Validator;
import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.exceptions.BadRequestException;
import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.dao.UserDAO;
import finalproject.youtube.model.dao.VideoDAO;
import finalproject.youtube.model.dto.PendingVideoDto;
import finalproject.youtube.model.dto.VideoDto;
import finalproject.youtube.model.pojo.*;
import finalproject.youtube.model.repository.CategoryRepository;
import finalproject.youtube.model.repository.UserRepository;
import finalproject.youtube.model.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VideoService {

    private static final int NUMBER_VIDEOS_PER_PAGE = 10;

    @Autowired
    VideoDAO videoDAO;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    AmazonClient amazonClient;

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    UserDAO userDAO;

    private void setInitialValuesToVideo(Video video) {
        video.setStatus(Video.Status.PENDING.toString());
        video.setVideoUrl("");
        video.setThumbnailUrl("");
        video.setNumberLikes(0);
        video.setNumberDislikes(0);
    }

    public PendingVideoDto uploadVideo(MultipartFile multipartFile,
                                       MultipartFile thumbnail,
                                       String title,
                                       String description,
                                       long categoryId,
                                       User owner) throws SQLException {
        Validator.validateVideoInformation(title);
        Video video = new Video();
        setInitialValuesToVideo(video);
        video.setTitle(title);
        video.setDescription(description);
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
        if (!optionalCategory.isPresent()) {
            throw new BadRequestException("Invalid category id!");
        }
        video.setCategory(optionalCategory.get());
        video.setOwner(owner);
        videoDAO.uploadVideo(video);
        try {
            Thread uploader = new Uploader(video, amazonClient.convertMultiPartToFile(multipartFile),
                    amazonClient.convertMultiPartToFile(thumbnail), amazonClient, videoRepository, userDAO);
            uploader.start();
        } catch (IOException e) {
            video.setStatus(Video.Status.FAILED.toString());
        }

        return video.toPendingVideoDto();
    }

    public Video validateAndGetVideo(long videoId) {
        Optional<Video> optionalVideo = videoRepository.findById(videoId);
        if (!optionalVideo.isPresent()) {
            throw new NotFoundException("Video not found!");
        }
        Video video = optionalVideo.get();
        String status = video.getStatus();
        if (status == null || !status.equals(Video.Status.UPLOADED.toString())) {
            throw new NotFoundException("Video not found!");
        }

        return video;
    }

    public VideoDto deleteVideo(long videoId, User loggedUser) {
        Video video  = validateAndGetVideo(videoId);
        if (video.getOwner().getId() != loggedUser.getId()) {
            throw new AuthorizationException("Unauthorized");
        }
        videoRepository.deleteById(videoId);
        amazonClient.deleteFileFromS3Bucket(video.getVideoUrl());
        amazonClient.deleteFileFromS3Bucket(video.getThumbnailUrl());

        return video.toVideoDto();
    }

    public VideoDto getVideoById(long videoId) {
        VideoDto video  = validateAndGetVideo(videoId).toVideoDto();

        return video;
    }

    public List<VideoDto> getVideosByTitle(String title, int page) {
        List<Video> videos = videoRepository.findAllByTitleContainingAndStatus(title, Video.Status.UPLOADED.toString(),
                PageRequest.of(page, NUMBER_VIDEOS_PER_PAGE));

        if (videos.isEmpty()) {
            throw new NotFoundException("No videos found!");
        }

        List<VideoDto> videoDtos = new ArrayList<>();
        for (Video video : videos) {
            videoDtos.add(video.toVideoDto());

        }

        return videoDtos;
    }

    public void likeVideo(long videoId, User loggedUser) throws SQLException {
        Video video = validateAndGetVideo(videoId);
        videoDAO.setReactionToVideo(video, loggedUser.getId(), VideoDAO.LIKE);
    }

    public void dislikeVideo(long videoId, User loggedUser) throws SQLException {
        Video video = validateAndGetVideo(videoId);
        videoDAO.setReactionToVideo(video, loggedUser.getId(), VideoDAO.DISLIKE);
    }

    public List<VideoDto> getAllByDateUploadedAndNumberLikes(int page) {
        List<Video> videos = videoRepository.findAllByStatusOrderByNumberLikesDescDateUploadedDesc(Video.Status.UPLOADED.toString(),
                PageRequest.of(page, NUMBER_VIDEOS_PER_PAGE));
        if (videos.isEmpty()) {
            throw new NotFoundException("Videos not found!");
        }
        List<VideoDto> videoDtos = new ArrayList<>();
        for (Video video : videos) {
            videoDtos.add(video.toVideoDto());
        }
        return videoDtos;
    }

}
