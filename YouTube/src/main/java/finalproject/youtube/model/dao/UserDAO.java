package finalproject.youtube.model.dao;

import finalproject.youtube.exceptions.BadRequestException;
import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.dto.NoPasswordUserDto;
import finalproject.youtube.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class UserDAO {

    @Autowired
    JdbcTemplate jdbcTemplate;

    // register user
    public long registerUser(User user) throws SQLException, BadRequestException {
        if (isDuplicateEmail(user.getEmail())) {
            throw new BadRequestException("Duplicate email!");
        }

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

    // check if the email is unique
    private boolean isDuplicateEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?;";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next() == false) {
                return false;
            }
            return true;
        }
    }

    // login user
    public int loginUser(String email, String password) throws SQLException, BadRequestException {
        String sql = "SELECT id FROM users WHERE email = ? AND password = ?;";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                throw new BadRequestException("Invalid email or password!");
            }
            int userId = resultSet.getInt(1);
            return userId;
        }
    }

    public User getById(long userId) throws SQLException, NotFoundException {
        String sql = "SELECT id, user_name, first_name, last_name, email, password, date_created " +
                "FROM users WHERE id = ?";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                throw new NotFoundException("No user with this id!");
            }
            String userName = resultSet.getString("user_name");
            String firstName = resultSet.getString("first_name");
            String lastName = resultSet.getString("last_name");
            String email = resultSet.getString("email");
            String password = resultSet.getString("password");
            LocalDateTime dateCreated = resultSet.getTimestamp("date_created").toLocalDateTime();

            User user = new User(userName, firstName, lastName, email, password);
            user.setId(userId);
            user.setDateCreated(dateCreated);
            return user;
        }
    }

    // edit user profile
    public void editProfile(User user) throws SQLException {
        String sql = "UPDATE users SET  user_name = ?, first_name = ?, last_name = ?, email = ?, password = md5(?) " +
                "WHERE id = ?;";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getFirstName());
            statement.setString(3, user.getLastName());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getPassword());
            statement.setLong(6, user.getId());
            statement.executeUpdate();
        }
    }

    // find all users with given username
    public List<NoPasswordUserDto> findByUsername(String username) throws SQLException {
        List<NoPasswordUserDto> users = new ArrayList<>();
        String sql = "SELECT id, user_name, first_name, last_name, email, password, date_created FROM users WHERE user_name = ?;";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
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

    }

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
