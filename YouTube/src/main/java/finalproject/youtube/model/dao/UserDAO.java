package finalproject.youtube.model.dao;

import finalproject.youtube.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class UserDAO {

    private static final String INSERT_IN_DB_SQL                 = "INSERT INTO users (user_name," +
            " first_name," +
            " last_name, " +
            "email," +
            " password," +
            " date_created)" +
            " VALUES (?, ?, ?, ?, ?, ?);";
    private static final String CHECK_IF_USER_HAS_SUBSCRIBED_SQL = "SELECT subscriber_id FROM subscriptions WHERE " +
            "subscriber_id = ? AND subscribed_to_id = ?;";
    private static final String SUBSCRIBE_TO_USER_SQL            = "INSERT INTO subscriptions (subscriber_id, subscribed_to_id) " +
            "VALUES (?, ?);";
    private static final String UNSUBSCRIBE_FROM_USER_SQL        = "DELETE FROM subscriptions WHERE subscriber_id = ? " +
            "AND subscribed_to_id = ?;";
    private static final String GET_USER_SUBSCRIBERS_SQL         =
            "SELECT u.email FROM subscriptions AS s " +
            "JOIN users AS u ON s.subscriber_id = u.id " +
            "WHERE s.subscribed_to_id = ?;";
    public static final  String VERIFICATION_CODE_WHERE_ID       =
            "UPDATE youtube.users SET verification_code = ? WHERE id=?;";
    public static final String VERIFY = "UPDATE youtube.users SET status = ? where verification_code = ? "; //todo come back again


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

    // add verification code to db
    public void addVerificationCode(User user, int verificationCode) throws SQLException {
        try(Connection connection = jdbcTemplate.getDataSource().getConnection()){
            try(PreparedStatement preparedStatement = connection.prepareStatement(VERIFICATION_CODE_WHERE_ID)){
                preparedStatement.setInt(1, verificationCode) ;
                preparedStatement.setLong(2, user.getId());
                preparedStatement.executeUpdate();
            }
        }
    }

    //verifies user
    public void verifyUser(User user, int verificationCode) throws SQLException {
        try(Connection connection = jdbcTemplate.getDataSource().getConnection()){
            try(PreparedStatement preparedStatement = connection.prepareStatement(VERIFY)){
               //todo
            }
        }
    }

}
