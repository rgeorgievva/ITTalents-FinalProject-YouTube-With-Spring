package finalproject.youtube.model.dao;

import finalproject.youtube.model.entity.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;

@Component
public class VideoDAO {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final int NUMBER_VIDEOS_PER_PAGE = 10;
    public static final int DISLIKE = -1;
    public static final int LIKE = 1;
    public static final String CHECK_IF_REACTION_EXISTS_SQL = "SELECT reaction FROM videos_reactions" +
            " WHERE user_id = ? AND video_id = ?;";
    private static final String UPLOAD_VIDEO_SQL = "INSERT INTO videos (title," +
            " description, " +
            "video_url," +
            " date_uploaded, " +
            "owner_id, " +
            "category_id," +
            " thumbnail_url, " +
            "status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String UPDATE_VIDEO_NUMBER_LIKES_SQL = "UPDATE videos SET number_likes = ? " +
            "WHERE id = ?;";
    private static final String INCREASE_VIDEO_NUMBER_DISLIKES_SQL = "UPDATE videos SET number_dislikes = ? " +
            "WHERE id = ?;";
    private static final String UPDATE_LIKES_AND_DISLIKES_TO_VIDEO = "UPDATE videos " +
            "SET number_likes = ?, " +
            "number_dislikes = ? " +
            " WHERE id = ?;";
    private static final String REACT_TO_VIDEO = "INSERT INTO videos_reactions (user_id, video_id, reaction) " +
            "VALUES (?, ?, ?);";
    private static final String REMOVE_REACTION_TO_VIDEO = "DELETE FROM videos_reactions WHERE " +
            "user_id = ? AND video_id = ?;";
    private static final String CHANGE_REACTION_TO_VIDEO = "UPDATE videos_reactions SET reaction = ? WHERE " +
            "user_id = ? AND video_id = ?;";
    private static final String GET_VIDEOS_ORDERED_BY_DATE_AND_NUMBER_LIKES = "SELECT * FROM videos " +
            "WHERE status='UPLOADED' " +
            "ORDER BY number_likes DESC, " +
            "date_uploaded DESC " +
            "LIMIT ? " +
            "OFFSET ?;";

    // add video
    public long uploadVideo(Video video) throws SQLException {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPLOAD_VIDEO_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, video.getTitle());
            statement.setString(2, video.getDescription());
            statement.setString(3, video.getVideoUrl());
            video.setDateUploaded(LocalDateTime.now());
            statement.setTimestamp(4, Timestamp.valueOf(video.getDateUploaded()));
            statement.setLong(5, video.getOwner().getId());
            statement.setLong(6, video.getCategory().getId());
            statement.setString(7, video.getThumbnailUrl());
            statement.setString(8, video.getStatus());
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            generatedKeys.next();
            video.setId(generatedKeys.getInt(1));

            return video.getId();
        }
    }

    // react to video
    public void setReactionToVideo(Video video, long userId, int reaction) throws SQLException {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(CHECK_IF_REACTION_EXISTS_SQL);
        ) {
            statement.setLong(1, userId);
            statement.setLong(2, video.getId());
            ResultSet resultSet = statement.executeQuery();
            // if there was no reaction to video
            if (!resultSet.next()) {
                // set the reaction the user wants
                reactToVideo(video, userId, reaction);
                return;
            }
            int currentReaction = resultSet.getInt("reaction");
            if (currentReaction != reaction) {
                //change reaction -> delete previous and set new reaction
                changeReactionToVideo(video, userId, reaction);
                return;
            }
            //remove previous reaction
            removeReactionToVideo(video, userId, reaction);
        }
    }

    private void reactToVideo(Video video, long userId, int reaction) throws SQLException {
        String sql;
        int updatedNumber;
        if (reaction == LIKE) {
            sql = UPDATE_VIDEO_NUMBER_LIKES_SQL;
            updatedNumber = video.getNumberLikes() + 1;
        }
        else {
            sql = INCREASE_VIDEO_NUMBER_DISLIKES_SQL;
            updatedNumber = video.getNumberDislikes() + 1;
        }
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        try (PreparedStatement statement1 = connection.prepareStatement(REACT_TO_VIDEO);
             PreparedStatement statement2 = connection.prepareStatement(sql);
        ) {
            connection.setAutoCommit(false);
            statement1.setLong(1, userId);
            statement1.setLong(2, video.getId());
            statement1.setInt(3, reaction);
            statement1.executeUpdate();

            statement2.setInt(1, updatedNumber);
            statement2.setLong(2, video.getId());
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

    private void changeReactionToVideo(Video video, long userId, int reaction) throws SQLException {
        int updatedNumberLikes;
        int updatedNumberDislikes;
        if (reaction == LIKE) {
            updatedNumberLikes = video.getNumberLikes() + 1;
            updatedNumberDislikes = video.getNumberDislikes() - 1;
        }
        else {
            updatedNumberLikes = video.getNumberLikes() - 1;
            updatedNumberDislikes = video.getNumberDislikes() + 1;
        }
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        try (PreparedStatement statement1 = connection.prepareStatement(CHANGE_REACTION_TO_VIDEO);
             PreparedStatement statement2 = connection.prepareStatement(UPDATE_LIKES_AND_DISLIKES_TO_VIDEO);
        ) {
            connection.setAutoCommit(false);
            statement1.setInt(1, reaction);
            statement1.setLong(2, userId);
            statement1.setLong(3, video.getId());
            statement1.executeUpdate();

            statement2.setInt(1, updatedNumberLikes);
            statement2.setInt(2, updatedNumberDislikes);
            statement2.setLong(3, video.getId());
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

    private void removeReactionToVideo(Video video, long userId, int reaction) throws SQLException {
        String sql;
        int updatedNumber;
        if (reaction == LIKE) {
            sql = UPDATE_VIDEO_NUMBER_LIKES_SQL;
            updatedNumber = video.getNumberLikes() - 1;
        }
        else {
            sql = INCREASE_VIDEO_NUMBER_DISLIKES_SQL;
            updatedNumber = video.getNumberDislikes() - 1;
        }
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        try (PreparedStatement statement1 = connection.prepareStatement(REMOVE_REACTION_TO_VIDEO);
             PreparedStatement statement2 = connection.prepareStatement(sql);
        ) {
            connection.setAutoCommit(false);
            statement1.setLong(1, userId);
            statement1.setLong(2, video.getId());
            statement1.executeUpdate();

            statement2.setInt(1, updatedNumber);
            statement2.setLong(2, video.getId());
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
}
