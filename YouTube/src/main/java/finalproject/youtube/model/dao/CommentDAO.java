package finalproject.youtube.model.dao;

import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.entity.Comment;
import finalproject.youtube.model.entity.User;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;

@Component
public class CommentDAO {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private CommentDAO(){}

    //todo fix
    @SneakyThrows
    public String likeComment(User user, Comment comment){
            try(Connection connection = jdbcTemplate.getDataSource().getConnection()) {
                //removes comment from liked comments
                if (commentIsAlreadyLiked(user, comment)) {
                    String unlike = "delete from youtube.users_liked_comments where user_id = ? and comment_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(unlike)) {
                        preparedStatement.setLong(1, user.getId());
                        preparedStatement.setLong(2, comment.getId());
                        preparedStatement.executeUpdate();
                    }
                    return "Removed like!";
                } else {
                    //removes comment from disliked comments
                    try {
                        connection.setAutoCommit(false);
                        if (commentIsAlreadyDisliked(user, comment)) {
                            String sql = "delete from youtube.users_disliked_comments where user_id = ? and comment_id = ?";
                            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                                preparedStatement.setLong(1, user.getId());
                                preparedStatement.setLong(2, comment.getId());
                                preparedStatement.executeUpdate();
                            }
                        }
                        //adds comment to liked comments
                        String like = "insert into youtube.users_liked_comments values (? , ?);";
                        try (PreparedStatement statement = connection.prepareStatement(like)) {
                            statement.setLong(1, user.getId());
                            statement.setLong(2, comment.getId());
                            statement.executeUpdate();
                        }

                        connection.commit();
                        connection.setAutoCommit(true);

                        return "Comment liked!";
                    }
                    catch (SQLException e){
                        connection.rollback();
                        throw  new SQLException("Connection rollback for liking comment!", e);
                    }
                }
            }
    }

    //todo fix
    @SneakyThrows
    public String dislikeComment(User user, Comment comment) {
        try(Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            //removes comment from disliked comments
            if (commentIsAlreadyDisliked(user, comment)) {
                String sql = "delete from youtube.users_disliked_comments where user_id = ? and comment_id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, user.getId());
                    preparedStatement.setLong(2, comment.getId());
                    preparedStatement.executeUpdate();
                }
                return "Removed dislike!";
            } else {
                //removes comment from liked comments
                try {
                    connection.setAutoCommit(false);
                    if (commentIsAlreadyLiked(user, comment)) {
                        String unlike = "delete from youtube.users_liked_comments where user_id = ? and comment_id = ?";
                        try (PreparedStatement preparedStatement = connection.prepareStatement(unlike)) {
                            preparedStatement.setLong(1, user.getId());
                            preparedStatement.setLong(2, comment.getId());
                            preparedStatement.executeUpdate();
                        }
                    }
                    //adds comment to disliked comments
                    String dislike = "insert into youtube.users_disliked_comments values (? , ?);";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(dislike)) {
                        preparedStatement.setLong(1, user.getId());
                        preparedStatement.setLong(2, comment.getId());
                        preparedStatement.executeUpdate();
                    }

                    connection.commit();
                    connection.setAutoCommit(true);
                    return "Comment disliked!";
                }
                catch (SQLException e){
                    connection.rollback();
                    throw new SQLException("Connection rollback for disliking comment!", e);
                }
            }
        }
    }
    //finds if the current comment is already liked
    private boolean commentIsAlreadyLiked(User user, Comment comment) throws SQLException {
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        String sql = "select * from youtube.users_liked_comments where user_id = ? and comment_id = ?;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setLong(2, comment.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    //finds if the current comment is already disliked
    private boolean commentIsAlreadyDisliked(User user, Comment comment) throws SQLException {
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        String sql = "select * from youtube.users_disliked_comments where user_id = ? and comment_id = ?;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setLong(2, comment.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }


}
