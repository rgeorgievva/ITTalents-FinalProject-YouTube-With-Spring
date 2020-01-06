package finalproject.youtube.model.dao;

import finalproject.youtube.db.DBManager;
import finalproject.youtube.exceptions.UserException;
import finalproject.youtube.model.dto.NoPasswordUserDto;
import finalproject.youtube.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class UserDAO {

    @Autowired
    DBManager dbManager;

    // register user
    public long registerUser(User user) throws UserException {
        try {
            if (isDuplicateEmail(user.getEmail())) {
                throw new UserException("Duplicate email!");
            }

            Connection connection = dbManager.getConnection();
            String sql = "INSERT INTO users (user_name, first_name, last_name, email, password, date_created)" +
                    " VALUES (?, ?, ?, ?, md5(?), ?);";
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        } catch (SQLException e) {
            throw new UserException("Could not register! Please try again later!", e);
        }
    }

    // check if the email is unique
    private boolean isDuplicateEmail(String email) throws SQLException {
        Connection connection = dbManager.getConnection();
        String sql = "SELECT * FROM users WHERE email = ?;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next() == false) {
                return false;
            }
            return true;
        }
    }

    // login user
    public int loginUser(String email, String password) throws UserException {
        try {
            Connection connection = dbManager.getConnection();
            String sql = "SELECT id FROM users WHERE email = ? AND password = md5(?);";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, email);
                statement.setString(2, password);
                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) {
                    throw new UserException("Invalid email or password!");
                }
                int userId = resultSet.getInt(1);
                return userId;
            }
        } catch (SQLException e) {
            throw new UserException("Could not login! Please try again later!", e);
        }
    }

    public User getById(long userId) throws UserException {
        try {
            Connection connection = dbManager.getConnection();
            String sql = "SELECT id, user_name, first_name, last_name, email, password, date_created " +
                    "FROM users WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, userId);
                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) {
                    throw new UserException("No user with this id!");
                }
                String userName = resultSet.getString("user_name");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");
                LocalDateTime dateCreated = resultSet.getTimestamp("date_created").toLocalDateTime();

                User user = new User(userName, firstName, lastName, email, password);
                user.setId(userId);
                return user;
            }
        } catch (SQLException | UserException e) {
            throw new UserException("Could not get this user. Please try again later", e);
        }
    }

    // edit user profile
    public void editProfile(User user) throws UserException {
        try {
            Connection connection = dbManager.getConnection();
            String sql = "UPDATE users SET  user_name = ?, first_name = ?, last_name = ?, email = ?, password = md5(?) " +
                    "WHERE id = ?;";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, user.getUsername());
                statement.setString(2, user.getFirstName());
                statement.setString(3, user.getLastName());
                statement.setString(4, user.getEmail());
                statement.setString(5, user.getPassword());
                statement.setLong(6, user.getId());
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new UserException("Could not edit profile. Please try again later.", e);
        }
    }

    // find all users with given username
    public List<NoPasswordUserDto> findByUsername(String username) throws UserException {
        try {
            List<NoPasswordUserDto> users = new ArrayList<>();
            Connection connection = dbManager.getConnection();
            String sql = "SELECT id, user_name, first_name, last_name, email, password, date_created FROM users WHERE user_name = ?;";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    long id = resultSet.getLong("id");
                    String userName = resultSet.getString("user_name");
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");
                    String email = resultSet.getString("email");
                    String password = resultSet.getString("password");
                    LocalDateTime dateCreated = resultSet.getTimestamp("date_created").toLocalDateTime();
                    User user = new User(userName, firstName, lastName, email, password);
                    user.setId(id);
                    user.setDateCreated(dateCreated);
                    users.add(user.toNoPasswordUserDto());
                }
            }
            return Collections.unmodifiableList(users);
        } catch (SQLException e) {
            throw new UserException("Could not get users with this username! Please try again later!", e);
        }
    }

    // subscribe to user
    public void subscribeToUser(User subscriber, long subscribedToId) throws UserException {
        try {
            Connection connection = dbManager.getConnection();
            String sql = "INSERT INTO subscriptions (subscriber_id, subscribed_to_id) VALUES (?, ?);";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, subscriber.getId());
                statement.setLong(2, subscribedToId);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new UserException("Could not subscribe. Please try again later!", e);
        }
    }

    // unsubscribe from user
    public void unsubscribeFromUser(User subscriber, long unsubscribeFromId) throws UserException {
        try {
            Connection connection = dbManager.getConnection();
            String sql = "DELETE FROM subscriptions WHERE subscriber_id = ? AND subscribed_to_id = ?;";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, subscriber.getId());
                statement.setLong(2, unsubscribeFromId);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new UserException("Could not unsubscribe. Please try again later!", e);
        }
    }
}
