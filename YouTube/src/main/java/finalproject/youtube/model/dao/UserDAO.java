package finalproject.youtube.model.dao;

import finalproject.youtube.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;

@Component
public class UserDAO {

    private static final String INSERT_IN_DB_SQL = "INSERT INTO users (user_name," +
            " first_name," +
            " last_name, " +
            "email," +
            " password," +
            " date_created)" +
            " VALUES (?, ?, ?, ?, ?, ?);";
    private static final String SUBSCRIBE_TO_USER_SQL = "INSERT INTO subscriptions (subscriber_id, subscribed_to_id) " +
            "VALUES (?, ?);";
    private static final String UNSUBSCRIBE_FROM_USER_SQL = "DELETE FROM subscriptions WHERE subscriber_id = ? " +
            "AND subscribed_to_id = ?;";


    @Autowired
    JdbcTemplate jdbcTemplate;

    // register user
    public long registerUser(User user) throws SQLException {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_IN_DB_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getFirstName());
            statement.setString(3, user.getLastName());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getPassword());
            user.setDateCreated(LocalDateTime.now());
            statement.setTimestamp(6, Timestamp.valueOf(user.getDateCreated()));
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            generatedKeys.next();
            user.setId(generatedKeys.getLong(1));

            return user.getId();
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
}
