package finalproject.youtube.model.dao;

import finalproject.youtube.db.DBManager;
import finalproject.youtube.exceptions.VideoException;
import finalproject.youtube.model.entity.User;
import finalproject.youtube.model.entity.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class VideoDAO {

    @Autowired
    DBManager dbManager;

    // add video
    public int uploadVideo(Video video) throws VideoException {
        try {
            Connection connection = dbManager.getConnection();
            String sql = "INSERT INTO videos (title, description, video_url, date_uploaded, owner_id, category_id," +
                    " duration, thumbnail_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, video.getTitle());
                statement.setString(2, video.getDescription());
                statement.setString(3, video.getVideoUrl());
                statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                statement.setInt(5, video.getOwnerId());
                statement.setInt(6, video.getCategoryId());
                statement.setLong(7, video.getDuration());
                statement.setString(8, video.getThumbnailUrl());
                statement.executeUpdate();
                ResultSet generatedKeys = statement.getGeneratedKeys();
                generatedKeys.next();
                int videoId = generatedKeys.getInt(1);
                return videoId;
            }

        } catch (SQLException e) {
            throw new VideoException("Could not upload video! Please try again later.", e);
        }
    }

    // remove video
    public void removeVideo(Video video) throws VideoException {
        try {
            Connection connection = dbManager.getConnection();
            String sql = "DELETE FROM videos WHERE id = ?;";
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, video.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new VideoException("Could not delete video! Please try again later.", e);
        }
    }

    // get video by id
    public Video getById(int id) throws VideoException {
        try {
            Connection connection = dbManager.getConnection();
            String sql = "SELECT id, title, description, video_url, date_uploaded, owner_id, category_id, duration, " +
                    "thumbnail_url FROM videos WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) {
                    throw new VideoException("No video with this id!");
                }
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                String url = resultSet.getString("video_url");
                LocalDateTime dateUploaded = resultSet.getTimestamp("date_uploaded").toLocalDateTime();
                int ownerId = resultSet.getInt("owner_id");
                int categoryId = resultSet.getInt("category_id");
                long duration = resultSet.getLong("duration");
                String thumbnailUrl = resultSet.getString("thumbnail_url");
                Video video = new Video(id, title, description, url, thumbnailUrl, duration, dateUploaded, ownerId,
                        categoryId);
                return video;
            }
        } catch (SQLException e) {
            throw new VideoException("Could not get this video. Please try again later", e);
        }
    }

    // get all videos by title
    public List<Video> getAllByTitle(String title) throws VideoException {
        List<Video> videos = new ArrayList<>();
        try {
            Connection connection = dbManager.getConnection();
            String sql = "SELECT id, title, description, video_url, date_uploaded, owner_id, category_id, duration, " +
                    "thumbnail_url FROM videos WHERE title = ?;";
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, title);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String videoTitle = resultSet.getString("title");
                    String description = resultSet.getString("description");
                    String url = resultSet.getString("video_url");
                    LocalDateTime dateUploaded = resultSet.getTimestamp("date_uploaded").toLocalDateTime();
                    int ownerId = resultSet.getInt("owner_id");
                    int categoryId = resultSet.getInt("category_id");
                    long duration = resultSet.getLong("duration");
                    String thumbnailUrl = resultSet.getString("thumbnail_url");
                    Video video = new Video(id, title, description, url, thumbnailUrl, duration, dateUploaded, ownerId,
                            categoryId);
                    videos.add(video);
                }
                if (videos.isEmpty()) {
                    throw new VideoException("No videos with title " + title + " found!");
                }
                return videos;
            }

        } catch (SQLException e) {
            throw new VideoException("Could not get videos with title " + title + "! Please try again later.", e);
        }
    }

    // like video
    public void likeVideo(Video video, User user) throws VideoException {
        try {
            // if the user has already liked this video -> remove like
            if (hasUserLikedVideo(user, video)) {
                removeLike(user, video);
                return;
            }

            // if the user has disliked this video -> remove the dislike and like the video
            if (hasUserDislikedVideo(user, video)) {
                removeDislikeAndLike(user, video);
                return;
            }

            // like the video
            Connection connection = dbManager.getConnection();
            String likeVideo = "INSERT INTO users_liked_videos (user_id, video_id) VALUES (?, ?);";
            try (PreparedStatement statement = connection.prepareStatement(likeVideo);
            ) {
                statement.setInt(1, user.getId());
                statement.setInt(2, video.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new VideoException("Could not like this video right now! Please try again later.", e);
        }
    }

    // dislike video
    public void dislikeVideo(Video video, User user) throws VideoException {
        try {
            // if the user has already disliked this video -> remove dislike
            if (hasUserDislikedVideo(user, video)) {
                removeDislike(user, video);
                return;
            }

            // if the user has liked this video -> remove the like and dislike the video
            if (hasUserLikedVideo(user, video)) {
                removeLikeAndDislike(user, video);
                return;
            }

            // dislike the video
            Connection connection = dbManager.getConnection();
            String dislikeVideo = "INSERT INTO users_disliked_videos (user_id, video_id) VALUES (?, ?);";
            try (PreparedStatement statement = connection.prepareStatement(dislikeVideo);
            ) {
                statement.setInt(1, user.getId());
                statement.setInt(2, video.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new VideoException("Could not dislike this video right now! Please try again later.", e);
        }
    }

    private boolean hasUserLikedVideo(User user, Video video) throws SQLException {
        String hasUserLikedVideo = "SELECT user_id, video_id FROM users_liked_videos WHERE user_id = ? AND video_id = ?;";

        return checkForReactionOfVideo(hasUserLikedVideo, user, video);
    }

    private boolean hasUserDislikedVideo(User user, Video video) throws SQLException {
        String hasUserDislikedVideo = "SELECT user_id, video_id FROM users_disliked_videos WHERE user_id = ? AND video_id = ?;";

        return checkForReactionOfVideo(hasUserDislikedVideo, user, video);
    }

    private boolean checkForReactionOfVideo(String sql, User user, Video video) throws SQLException {
        Connection connection = dbManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, user.getId());
            statement.setInt(2, video.getId());
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return false;
            }
            return true;
        }
    }

    private void removeDislike(User user, Video video) throws SQLException {
        Connection connection = dbManager.getConnection();
        String sql = "DELETE FROM users_disliked_videos WHERE user_id = ? AND video_id = ?;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, user.getId());
            statement.setInt(2, video.getId());
            statement.executeUpdate();
        }
    }

    private void removeLike(User user, Video video) throws SQLException {
        Connection connection = dbManager.getConnection();
        String sql = "DELETE FROM users_liked_videos WHERE user_id = ? AND video_id = ?;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, user.getId());
            statement.setInt(2, video.getId());
            statement.executeUpdate();
        }
    }

    private void removeDislikeAndLike(User user, Video video) throws SQLException {
        String removeDislike = "DELETE FROM users_disliked_videos WHERE user_id = ? AND video_id = ?;";
        String likeVideo = "INSERT INTO users_liked_videos (user_id, video_id) VALUES (?, ?);";

        executeTwoUpdatesInTransaction(removeDislike, likeVideo, user, video);
    }

    private void removeLikeAndDislike(User user, Video video) throws SQLException {
        String removeLike = "DELETE FROM users_liked_videos WHERE user_id = ? AND video_id = ?;";
        String dislikeVideo = "INSERT INTO users_disliked_videos (user_id, video_id) VALUES (?, ?);";

        executeTwoUpdatesInTransaction(removeLike, dislikeVideo, user, video);
    }

    private void executeTwoUpdatesInTransaction(String sql1, String sql2, User user, Video video) throws SQLException {
        Connection connection = dbManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql1);
             PreparedStatement statement2 = connection.prepareStatement(sql2);
        ) {
            connection.setAutoCommit(false);

            statement.setInt(1, user.getId());
            statement.setInt(2, video.getId());
            statement.executeUpdate();

            statement2.setInt(1, user.getId());
            statement2.setInt(2, video.getId());
            statement2.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // get all videos uploaded by user
    public List<Video> getAllVideosByOwner(User user) throws VideoException {
        List<Video> videosByOwner = new ArrayList<>();
        try {
            Connection connection = dbManager.getConnection();
            String sql = "SELECT id, title, description, video_url, date_uploaded, owner_id, category_id, duration, " +
                    "thumbnail_url FROM videos WHERE owner_id = ?;";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, user.getId());
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String videoTitle = resultSet.getString("title");
                    String description = resultSet.getString("description");
                    String url = resultSet.getString("video_url");
                    LocalDateTime dateUploaded = resultSet.getTimestamp("date_uploaded").toLocalDateTime();
                    int ownerId = resultSet.getInt("owner_id");
                    int categoryId = resultSet.getInt("category_id");
                    long duration = resultSet.getLong("duration");
                    String thumbnailUrl = resultSet.getString("thumbnail_url");
                    Video video = new Video(id, videoTitle, description, url, thumbnailUrl, duration, dateUploaded, ownerId,
                            categoryId);
                    videosByOwner.add(video);
                }
                if (videosByOwner.isEmpty()) {
                    throw new VideoException("User " + user.getUsername() + " has no uploaded videos!");
                }
                return videosByOwner;
            }
        } catch (SQLException e) {
            throw new VideoException("Could not get videos uploaded by " + user.getUsername() +
                    ". Please try again later.", e);
        }
    }

    // get all videos sorted by time uploaded and number likes
    public List<Video> getAllByDateUploadedAndNumberLikes() throws VideoException {
        try {
            List<Video> videos = new ArrayList<>();
            Connection connection = dbManager.getConnection();
            String sql = "SELECT v.*, COUNT(*) AS total_likes " +
                    "FROM users_liked_videos AS l " +
                    "JOIN videos AS v ON l.video_id = v.id " +
                    "GROUP BY l.video_id " +
                    "ORDER BY DATE(v.date_uploaded) DESC, total_likes DESC;";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet result = statement.executeQuery();
                while (result.next()) {
                    Video video = new Video(result.getInt("id"),
                            result.getString("title"),
                            result.getString("description"),
                            result.getString("video_url"),
                            result.getString("thumbnail_url"),
                            result.getLong("duration"),
                            result.getTimestamp("date_uploaded").toLocalDateTime(),
                            result.getInt("owner_id"),
                            result.getInt("category_id"));
                    videos.add(video);
                }
                if (videos.isEmpty()) {
                    throw new VideoException("No videos uploaded!");
                }
                return Collections.unmodifiableList(videos);
            }
        } catch (SQLException e) {
            throw new VideoException("Could not get all videos by uploading date and number likes. Please " +
                    "try again later.", e);
        }
    }
}
