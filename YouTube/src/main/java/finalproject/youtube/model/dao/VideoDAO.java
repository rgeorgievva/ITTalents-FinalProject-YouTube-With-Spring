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

    private static final String UPLOAD_VIDEO_SQL = "INSERT INTO videos (title, description, video_url," +
            " date_uploaded, owner_id, category_id," +
            " thumbnail_url, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String LIKE_VIDEO_SQL = "INSERT INTO users_liked_videos (user_id, video_id) VALUES (?, ?);";
    private static final String DISLIKE_VIDEO_SQL = "INSERT INTO users_disliked_videos (user_id, video_id) " +
            "VALUES (?, ?);";
    private static final String HAS_USER_LIKED_VIDEO = "SELECT user_id, video_id FROM users_liked_videos WHERE " +
            "user_id = ? AND video_id = ?;";
    private static final String HAS_USER_DISLIKED_VIDEO = "INSERT INTO users_disliked_videos (user_id, video_id) " +
            "VALUES (?, ?);";
    private static final String REMOVE_LIKE_SQL = "DELETE FROM users_liked_videos " +
            "WHERE user_id = ? AND video_id = ?;";
    private static final String REMOVE_DISLIKE_SQL = "DELETE FROM users_disliked_videos" +
            " WHERE user_id = ? AND video_id = ?;";
    private static final String GET_VIDEOS_ORDERED_BY_DATE_AND_NUMBER_LIKES = "SELECT v.*, COUNT(*) AS total_likes " +
            "FROM users_liked_videos AS l " +
            "JOIN videos AS v ON l.video_id = v.id " +
            "GROUP BY l.video_id " +
            "ORDER BY DATE(v.date_uploaded) DESC, total_likes DESC;";

    // add video
    public int uploadVideo(Video video) throws SQLException {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPLOAD_VIDEO_SQL, Statement.RETURN_GENERATED_KEYS)) {
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
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(LIKE_VIDEO_SQL);
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
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(DISLIKE_VIDEO_SQL);
        ) {
            statement.setLong(1, user.getId());
            statement.setLong(2, videoId);
            statement.executeUpdate();
        }
    }

    private boolean hasUserLikedVideo(User user, long videoId) throws SQLException {
        return checkForReactionOfVideo(HAS_USER_LIKED_VIDEO, user, videoId);
    }

    private boolean hasUserDislikedVideo(User user, long videoId) throws SQLException {
        return checkForReactionOfVideo(HAS_USER_DISLIKED_VIDEO, user, videoId);
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
        executeTwoUpdatesInTransaction(REMOVE_DISLIKE_SQL, LIKE_VIDEO_SQL, user, videoId);
    }

    private void removeLikeAndDislike(User user, long videoId) throws SQLException {
        executeTwoUpdatesInTransaction(REMOVE_LIKE_SQL, DISLIKE_VIDEO_SQL, user, videoId);
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

    // get all videos sorted by time uploaded and number likes
    public List<Video> getAllByDateUploadedAndNumberLikes() throws SQLException {
        List<Video> videos = new ArrayList<>();
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_VIDEOS_ORDERED_BY_DATE_AND_NUMBER_LIKES)) {
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

            return videos;
        }
    }

}
