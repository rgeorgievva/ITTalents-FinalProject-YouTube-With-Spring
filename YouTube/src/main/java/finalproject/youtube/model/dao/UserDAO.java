package finalproject.youtube.model.dao;

import finalproject.youtube.model.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserDAO {

    private static final String CHECK_IF_USER_HAS_SUBSCRIBED_SQL = "SELECT subscriber_id " +
            "FROM subscriptions WHERE " +
            "subscriber_id = ? AND " +
            "subscribed_to_id = ?;";
    private static final String SUBSCRIBE_TO_USER_SQL = "INSERT INTO subscriptions " +
            "(subscriber_id, subscribed_to_id) " +
            "VALUES (?, ?);";
    private static final String UNSUBSCRIBE_FROM_USER_SQL = "DELETE FROM subscriptions " +
            "WHERE subscriber_id = ? " +
            "AND subscribed_to_id = ?;";
    private static final String GET_USER_SUBSCRIBERS_SQL =
            "SELECT u.email FROM subscriptions AS s " +
            "JOIN users AS u ON s.subscriber_id = u.id " +
            "WHERE s.subscribed_to_id = ?;";


    @Autowired
    JdbcTemplate jdbcTemplate;

    // check if user has subscribed to another user
    public boolean hasSubscribedTo(long subscriberId, long subscribedToId) throws SQLException {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(CHECK_IF_USER_HAS_SUBSCRIBED_SQL)) {
            statement.setLong(1, subscriberId);
            statement.setLong(2, subscribedToId);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return false;
            }
            return true;
        }
    }

    // subscribe to user
    public void subscribeToUser(User subscriber, long subscribedToId) throws SQLException {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(SUBSCRIBE_TO_USER_SQL)) {
            statement.setLong(1, subscriber.getId());
            statement.setLong(2, subscribedToId);
            statement.executeUpdate();
        }
    }

    // unsubscribe from user
    public void unsubscribeFromUser(User subscriber, long unsubscribeFromId) throws SQLException {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(UNSUBSCRIBE_FROM_USER_SQL)) {
            statement.setLong(1, subscriber.getId());
            statement.setLong(2, unsubscribeFromId);
            statement.executeUpdate();
        }
    }

    public List<String> getSubscribers(long subscribedToId) throws SQLException {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_USER_SUBSCRIBERS_SQL)) {
            statement.setLong(1, subscribedToId);
            ResultSet resultSet = statement.executeQuery();
            List<String> subscribersEmails = new ArrayList<>();
            while (resultSet.next()) {
                subscribersEmails.add(resultSet.getString("email"));
            }
            return subscribersEmails;
        }
    }
}
