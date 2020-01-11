package finalproject.youtube.model.entity;

import finalproject.youtube.AmazonClient;
import finalproject.youtube.model.dao.UserDAO;
import finalproject.youtube.model.mail.VideoUploadNotificator;
import finalproject.youtube.model.repository.VideoRepository;

import java.io.File;


public class Uploader extends Thread {

    private Video video;
    private File multipartFile;
    private File thumbnail;
    private AmazonClient amazonClient;
    private VideoRepository videoRepository;
    private UserDAO userDAO;

    public Uploader(Video video, File multipartFile, File thumbnail, AmazonClient amazonClient,
                    VideoRepository videoRepository, UserDAO userDAO) {
        this.video = video;
        this.multipartFile = multipartFile;
        this.thumbnail = thumbnail;
        this.amazonClient = amazonClient;
        this.videoRepository = videoRepository;
        this.userDAO = userDAO;
    }

    @Override
    public void run() {
        String videoUrl = amazonClient.uploadFile(multipartFile, video, false);
        String thumbnailUrl = amazonClient.uploadFile(thumbnail, video, true);
        video.setVideoUrl(videoUrl);
        video.setThumbnailUrl(thumbnailUrl);
        if (!video.getStatus().equals(Status.FAILED.toString())) {
            video.setStatus(Status.UPLOADED.toString());
        }
        videoRepository.save(video);
        Thread notificator = new VideoUploadNotificator(userDAO, video.getOwner());
        notificator.start();
    }
}
