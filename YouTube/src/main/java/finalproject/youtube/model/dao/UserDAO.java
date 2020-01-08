package finalproject.youtube.model.dao;

import finalproject.youtube.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;

@Component
public class UserDAO {

    @Autowired
    JdbcTemplate jdbcTemplate;

    // register user
    public long registerUser(User user) throws SQLException {
        String sql = "INSERT INTO users (user_name, first_name, last_name, email, password, date_created)" +
                " VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
            long userId = generatedKeys.getLong(1);
            return userId;
        }
    }

//
//    // edit user profile
//    public void editProfile(User user) throws SQLException {
//        String sql = "UPDATE users SET  user_name = ?, first_name = ?, last_name = ?, email = ?, password = md5(?) " +
//                "WHERE id = ?;";
//        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
//             PreparedStatement statement = connection.prepareStatement(sql)) {
//            statement.setString(1, user.getUsername());
//            statement.setString(2, user.getFirstName());
//            statement.setString(3, user.getLastName());
//            statement.setString(4, user.getEmail());
//            statement.setString(5, user.getPassword());
//            statement.setLong(6, user.getId());
//            statement.executeUpdate();
//        }
//    }

    // subscribe to user
    public void subscribeToUser(User subscriber, long subscribedToId) throws SQLException {
        String sql = "INSERT INTO subscriptions (subscriber_id, subscribed_to_id) VALUES (?, ?);";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, subscriber.getId());
            statement.setLong(2, subscribedToId);
            statement.executeUpdate();
        }
    }

    // unsubscribe from user
    public void unsubscribeFromUser(User subscriber, long unsubscribeFromId) throws SQLException {
        String sql = "DELETE FROM subscriptions WHERE subscriber_id = ? AND subscribed_to_id = ?;";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, subscriber.getId());
            statement.setLong(2, unsubscribeFromId);
            statement.executeUpdate();
        }

    }
}
