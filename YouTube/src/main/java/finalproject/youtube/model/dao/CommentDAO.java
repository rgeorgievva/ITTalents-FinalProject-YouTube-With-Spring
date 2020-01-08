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

    @SneakyThrows
    public Comment getCommentById(long id) {
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        String sql = "select * from youtube.comments where id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet resultSet = ps.executeQuery();
            if(!resultSet.next()){
                throw new NotFoundException("There is no such comment with id=" + id);
            }
            String text = resultSet.getString("text");
            LocalDateTime time = resultSet.getTimestamp("time_posted").toLocalDateTime();
            long videoId = resultSet.getLong("video_id");
            long ownerid = resultSet.getLong("owner_id");
            long repliedToId = resultSet.getLong("replied_to_id");
            Comment comment = new Comment(id,text,time,videoId,ownerid,repliedToId);
            return comment;
        }
    }

    @SneakyThrows
    public void addCommentToVideo(Comment comment) {
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        String sql = "insert into youtube.comments " +
                "(text, time_posted, video_id, owner_id) values" +
                "(?,?,?,?);";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, comment.getText());
            preparedStatement.setTimestamp(2, Timestamp.valueOf(comment.getTimePosted()));
            preparedStatement.setLong(3, comment.getVideoId());
            preparedStatement.setLong(4, comment.getOwnerId());
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            int comment_id = resultSet.getInt(1);
            comment.setId(comment_id);
        }
    }

    @SneakyThrows
    public void addReplyToComment(Comment reply){
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        String sql = "insert into youtube.comments " +
                "(text, time_posted, video_id, owner_id, replied_to_id) values" +
                "(?,?,?,?,?);";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, reply.getText());
            reply.setTimePosted(LocalDateTime.now());
            preparedStatement.setTimestamp(2, Timestamp.valueOf(reply.getTimePosted()));
            preparedStatement.setLong(3, reply.getVideoId());
            preparedStatement.setLong(4, reply.getOwnerId());
            preparedStatement.setLong(5, reply.getRepliedToId());
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            int comment_id = resultSet.getInt(1);
            reply.setId(comment_id);
        }
    }

    @SneakyThrows
    public void editComment(Comment comment){
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        String sql = "update youtube.comments set text = ? where id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, comment.getText());
            preparedStatement.setLong(2, comment.getId());
            preparedStatement.executeUpdate();
        }
    }

    @SneakyThrows
    public void deleteComment(Comment comment){
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        String deleteFromComments = "delete from youtube.comments where id = ?;";
        try (PreparedStatement deleteFromCommentsStatement = connection.prepareStatement(deleteFromComments);) {
            deleteFromCommentsStatement.setLong(1, comment.getId());
            deleteFromCommentsStatement.executeUpdate();
            connection.commit();
        }
    }

    @SneakyThrows
    public void likeComment(User user, Comment comment){
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            //removes comment from liked comments
            if(commentIsAlreadyLiked(user, comment)){
                String unlike = "delete from youtube.users_liked_comments where user_id = ? and comment_id = ?";
                try(PreparedStatement preparedStatement = connection.prepareStatement(unlike)) {
                    preparedStatement.setLong(1, user.getId());
                    preparedStatement.setLong(2, comment.getId());
                    preparedStatement.executeUpdate();
                }
            }
            else {
                //removes comment from disliked comments
                    connection.setAutoCommit(false);
                    if (commentIsAlreadyDisliked(user, comment)) {
                        String sql = "delete from youtube.users_disliked_comments where user_id = ? and comment_id = ?";
                        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                            preparedStatement.setLong(1, user.getId());
                            preparedStatement.setLong(2, comment.getId());
                            preparedStatement.executeUpdate();
                        }
                    }
                    //adds comment to liked comments
                    String like = "insert into youtube.users_liked_comments values (? , ?);";
                    try(PreparedStatement statement = connection.prepareStatement(like)) {
                        statement.setLong(1, user.getId());
                        statement.setLong(2, comment.getId());
                        statement.executeUpdate();
                        connection.commit();
                    }
            }
    }

    @SneakyThrows
    public void dislikeComment(User user, Comment comment) {
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        //removes comment from disliked comments
        if (commentIsAlreadyDisliked(user, comment)) {
            String sql = "delete from youtube.users_disliked_comments where user_id = ? and comment_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, user.getId());
                preparedStatement.setLong(2, comment.getId());
                preparedStatement.executeUpdate();
            }
        } else {
            //removes comment from liked comments
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
