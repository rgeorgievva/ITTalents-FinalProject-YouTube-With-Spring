package finalproject.youtube.model.dao;

import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.entity.Status;
import finalproject.youtube.model.entity.User;
import finalproject.youtube.model.entity.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class VideoDAO {

    @Autowired
    JdbcTemplate jdbcTemplate;

    // add video
    public int uploadVideo(Video video) throws SQLException {
        String sql = "INSERT INTO videos (title, description, video_url, date_uploaded, owner_id, category_id," +
                " thumbnail_url, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, video.getTitle());
            statement.setString(2, video.getDescription());
            statement.setString(3, video.getVideoUrl());
            video.setDateUploaded(LocalDateTime.now());
            statement.setTimestamp(4, Timestamp.valueOf(video.getDateUploaded()));
            statement.setLong(5, video.getOwnerId());
            statement.setLong(6, video.getCategoryId());
            statement.setString(7, video.getThumbnailUrl());
            statement.setString(8, video.getStatus());
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            generatedKeys.next();
            int videoId = generatedKeys.getInt(1);

            return videoId;
        }
    }

    // remove video
    public void removeVideo(long videoId) throws SQLException {
        String sql = "DELETE FROM videos WHERE id = ?;";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, videoId);
            statement.executeUpdate();
        }
    }

    // get video by id
    public Video getById(long id) throws SQLException, NotFoundException {
        String sql = "SELECT id, title, description, video_url, date_uploaded, owner_id, category_id, " +
                "thumbnail_url, status FROM videos WHERE id = ?";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException("No video with this id found!");
            }

            Video video = new Video();
            video.setTitle(resultSet.getString("title"));
            video.setDescription(resultSet.getString("description"));
            video.setVideoUrl(resultSet.getString("video_url"));
            video.setDateUploaded(resultSet.getTimestamp("date_uploaded").toLocalDateTime());
            video.setOwnerId(resultSet.getLong("owner_id"));
            video.setCategoryId(resultSet.getInt("category_id"));
            video.setThumbnailUrl(resultSet.getString("thumbnail_url"));
            video.setStatus(resultSet.getString("status"));

            return video;
        }
    }

    // get all videos by title
    public List<Video> getAllByTitle(String title) throws SQLException, NotFoundException {
        List<Video> videos = new ArrayList<>();
        String sql = "SELECT id, title, description, video_url, date_uploaded, owner_id, category_id, " +
                "thumbnail_url, status FROM videos WHERE title = ?;";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, title);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String description = resultSet.getString("description");
                String url = resultSet.getString("video_url");
                LocalDateTime dateUploaded = resultSet.getTimestamp("date_uploaded").toLocalDateTime();
                long ownerId = resultSet.getLong("owner_id");
                long categoryId = resultSet.getLong("category_id");
                String thumbnailUrl = resultSet.getString("thumbnail_url");
                String status = resultSet.getString("status");

                Video video = new Video(id, title, description, url, thumbnailUrl, dateUploaded, ownerId,
                        categoryId, status);

                videos.add(video);
            }

            if (videos.isEmpty()) {
                throw new NotFoundException("No videos with title " + title + " found!");
            }

            return videos;
        }
    }

    // like video
    public void likeVideo(long videoId, User user) throws SQLException {
        // if the user has already liked this video -> remove like
        if (hasUserLikedVideo(user, videoId)) {
            removeLike(user, videoId);
            return;
        }

        // if the user has disliked this video -> remove the dislike and like the video
        if (hasUserDislikedVideo(user, videoId)) {
            removeDislikeAndLike(user, videoId);
            return;
        }

        // like the video
        String likeVideo = "INSERT INTO users_liked_videos (user_id, video_id) VALUES (?, ?);";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(likeVideo);
        ) {
            statement.setLong(1, user.getId());
            statement.setLong(2, videoId);
            statement.executeUpdate();
        }
    }

    // dislike video
    public void dislikeVideo(long videoId, User user) throws SQLException {
        // if the user has already disliked this video -> remove dislike
        if (hasUserDislikedVideo(user, videoId)) {
            removeDislike(user, videoId);
            return;
        }

        // if the user has liked this video -> remove the like and dislike the video
        if (hasUserLikedVideo(user, videoId)) {
            removeLikeAndDislike(user, videoId);
            return;
        }

        // dislike the video
        String dislikeVideo = "INSERT INTO users_disliked_videos (user_id, video_id) VALUES (?, ?);";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(dislikeVideo);
        ) {
            statement.setLong(1, user.getId());
            statement.setLong(2, videoId);
            statement.executeUpdate();
        }
    }

    private boolean hasUserLikedVideo(User user, long videoId) throws SQLException {
        String hasUserLikedVideo = "SELECT user_id, video_id FROM users_liked_videos WHERE user_id = ? AND video_id = ?;";

        return checkForReactionOfVideo(hasUserLikedVideo, user, videoId);
    }

    private boolean hasUserDislikedVideo(User user, long videoId) throws SQLException {
        String hasUserDislikedVideo = "SELECT user_id, video_id FROM users_disliked_videos WHERE user_id = ? AND video_id = ?;";

        return checkForReactionOfVideo(hasUserDislikedVideo, user, videoId);
    }

    private boolean checkForReactionOfVideo(String sql, User user, long videoId) throws SQLException {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, user.getId());
            statement.setLong(2, videoId);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return false;
            }
            return true;
        }
    }

    private void removeDislike(User user, long videoId) throws SQLException {
        String sql = "DELETE FROM users_disliked_videos WHERE user_id = ? AND video_id = ?;";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, user.getId());
            statement.setLong(2, videoId);
            statement.executeUpdate();
        }
    }

    private void removeLike(User user, long videoId) throws SQLException {
        String sql = "DELETE FROM users_liked_videos WHERE user_id = ? AND video_id = ?;";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, user.getId());
            statement.setLong(2, videoId);
            statement.executeUpdate();
        }
    }

    private void removeDislikeAndLike(User user, long videoId) throws SQLException {
        String removeDislike = "DELETE FROM users_disliked_videos WHERE user_id = ? AND video_id = ?;";
        String likeVideo = "INSERT INTO users_liked_videos (user_id, video_id) VALUES (?, ?);";

        executeTwoUpdatesInTransaction(removeDislike, likeVideo, user, videoId);
    }

    private void removeLikeAndDislike(User user, long videoId) throws SQLException {
        String removeLike = "DELETE FROM users_liked_videos WHERE user_id = ? AND video_id = ?;";
        String dislikeVideo = "INSERT INTO users_disliked_videos (user_id, video_id) VALUES (?, ?);";

        executeTwoUpdatesInTransaction(removeLike, dislikeVideo, user, videoId);
    }

    private void executeTwoUpdatesInTransaction(String sql1, String sql2, User user, long videoId) throws SQLException {
        Connection connection = jdbcTemplate.getDataSource().getConnection();

        try (PreparedStatement statement = connection.prepareStatement(sql1);
             PreparedStatement statement2 = connection.prepareStatement(sql2);
        ) {
            connection.setAutoCommit(false);

            statement.setLong(1, user.getId());
            statement.setLong(2, videoId);
            statement.executeUpdate();

            statement2.setLong(1, user.getId());
            statement2.setLong(2, videoId);
            statement2.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
            connection.close();
        }
    }

    // get all videos uploaded by user
    public List<Video> getAllVideosByOwner(User user) throws SQLException, NotFoundException {
        List<Video> videosByOwner = new ArrayList<>();
        String sql = "SELECT id, title, description, video_url, date_uploaded, owner_id, category_id, " +
                "thumbnail_url, status FROM videos WHERE owner_id = ?;";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, user.getId());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String videoTitle = resultSet.getString("title");
                String description = resultSet.getString("description");
                String url = resultSet.getString("video_url");
                LocalDateTime dateUploaded = resultSet.getTimestamp("date_uploaded").toLocalDateTime();
                long ownerId = resultSet.getLong("owner_id");
                int categoryId = resultSet.getInt("category_id");
                String thumbnailUrl = resultSet.getString("thumbnail_url");
                String  status = resultSet.getString("status");

                Video video = new Video(id, videoTitle, description, url, thumbnailUrl, dateUploaded, ownerId,
                        categoryId, status);
                videosByOwner.add(video);
            }
            if (videosByOwner.isEmpty()) {
                throw new NotFoundException("User " + user.getUsername() + " has no uploaded videos!");
            }
            return videosByOwner;
        }
    }

    // get all videos sorted by time uploaded and number likes
    public List<Video> getAllByDateUploadedAndNumberLikes() throws NotFoundException, SQLException {
        List<Video> videos = new ArrayList<>();
        String sql = "SELECT v.*, COUNT(*) AS total_likes " +
                "FROM users_liked_videos AS l " +
                "JOIN videos AS v ON l.video_id = v.id " +
                "GROUP BY l.video_id " +
                "ORDER BY DATE(v.date_uploaded) DESC, total_likes DESC;";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                Video video = new Video(result.getLong("id"),
                        result.getString("title"),
                        result.getString("description"),
                        result.getString("video_url"),
                        result.getString("thumbnail_url"),
                        result.getTimestamp("date_uploaded").toLocalDateTime(),
                        result.getLong("owner_id"),
                        result.getLong("category_id"),
                        result.getString("status")
                );
                videos.add(video);
            }
            if (videos.isEmpty()) {
                throw new NotFoundException("No videos uploaded!");
            }
            return Collections.unmodifiableList(videos);
        }
    }

}
