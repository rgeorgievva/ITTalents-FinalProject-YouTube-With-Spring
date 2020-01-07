package finalproject.youtube.model.dao;

import finalproject.youtube.exceptions.CommentException;
import finalproject.youtube.model.entity.Comment;
import finalproject.youtube.model.entity.User;
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

    public Comment getCommentById(long id) throws CommentException {
        try{
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            String sql = "select * from youtube.comments where id = ?;";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, id);
                ResultSet resultSet = ps.executeQuery();
                if(!resultSet.next()){
                    throw new CommentException("There is no such comment with id=" + id);
                }
                String text = resultSet.getString("text");
                LocalDateTime time = resultSet.getTimestamp("time_posted").toLocalDateTime();
                long videoId = resultSet.getLong("video_id");
                long ownerid = resultSet.getLong("owner_id");
                long repliedToId = resultSet.getLong("replied_to_id");
                Comment comment = new Comment(id,text,time,videoId,ownerid,repliedToId);
                return comment;
                }
            }catch (SQLException e) {
                throw new CommentException("There is no such comment with id=" + id, e);
            }
    }

    public void addCommentToVideo(Comment comment) throws CommentException {
        try {
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            String sql = "insert into youtube.comments " +
                    "(text, time_posted, video_id, owner_id) values" +
                    "(?,?,?,?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, comment.getText());
                preparedStatement.setTimestamp(2, Timestamp.valueOf(comment.getTimePosted()));
                preparedStatement.setLong(3, comment.getVideoId());
                preparedStatement.setLong(4,  comment.getOwnerId());
                preparedStatement.executeUpdate();
                ResultSet resultSet = preparedStatement.getGeneratedKeys();
                resultSet.next();
                int comment_id = resultSet.getInt(1);
                comment.setId(comment_id);
        }catch (SQLException e) {
            throw new CommentException("Could not add comment to video. Please, try again later.", e);
        }
    }

    public void addReplyToComment(Comment reply) throws CommentException{
        try {
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            String sql = "insert into youtube.comments " +
                    "(text, time_posted, video_id, owner_id, replied_to_id) values" +
                    "(?,?,?,?,?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, reply.getText());
            reply.setTimePosted(LocalDateTime.now());
                preparedStatement.setTimestamp(2, Timestamp.valueOf(reply.getTimePosted()));
                preparedStatement.setLong(3, reply.getVideoId());
                preparedStatement.setLong(4,  reply.getOwnerId());
                preparedStatement.setLong(5,  reply.getRepliedToId());
                preparedStatement.executeUpdate();
                ResultSet resultSet = preparedStatement.getGeneratedKeys();
                resultSet.next();
                int comment_id = resultSet.getInt(1);
                reply.setId(comment_id);
        } catch (SQLException e) {
            throw  new CommentException("Could not add reply to comment. Please, try again later.", e);
        }
    }

    public void editComment(Comment comment) throws CommentException {
        try {
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            String sql = "update youtube.comments set text = ? where id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, comment.getText());
                preparedStatement.setLong(2, comment.getId());
                preparedStatement.executeUpdate();
        } catch (SQLException e) {
           throw new CommentException("Could not edit comment. Please, try again later.", e);
        }
    }

    public void deleteComment(Comment comment) throws CommentException {
        try{
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            String deleteFromComments = "delete from youtube.comments where id = ?;";

            try (PreparedStatement deleteFromCommentsStatement = connection.prepareStatement(deleteFromComments);) {

                connection.setAutoCommit(false);

                deleteFromCommentsStatement.setLong(1, comment.getId());
                deleteFromCommentsStatement.executeUpdate();

                connection.commit();

            }catch (SQLException e) {
                connection.rollback();
                throw new CommentException("Something went wrong with deleting comment or its reactions", e);
            }
            finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new CommentException("Comment couldn't be deleted", e);
        }
    }

    public void likeComment(User user, Comment comment) throws CommentException {
        try {
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            //removes comment from liked comments
            if(commentIsAlreadyLiked(user, comment)){
                String unlike = "delete from youtube.users_liked_comments where user_id = ? and comment_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(unlike);
                preparedStatement.setLong(1, user.getId());
                preparedStatement.setLong(2, comment.getId());
                preparedStatement.executeUpdate();
            }
            else {
                //removes comment from disliked comments
                if(commentIsAlreadyDisliked(user, comment)){
                    String sql = "delete from youtube.users_disliked_comments where user_id = ? and comment_id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setLong(1, user.getId());
                    preparedStatement.setLong(2, comment.getId());
                    preparedStatement.executeUpdate();
                }
                //adds comment to liked comments
                String like = "insert into youtube.users_liked_comments values (? , ?);";
                PreparedStatement preparedStatement = connection.prepareStatement(like);
                preparedStatement.setLong(1, user.getId());
                preparedStatement.setLong(2, comment.getId());
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new CommentException("Could not like comment. Please, try again later.",e);
        }
    }

    public void dislikeComment(User user, Comment comment) throws CommentException {
        try {
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            //removes comment from disliked comments
            if(commentIsAlreadyDisliked(user, comment)){
                String sql = "delete from youtube.users_disliked_comments where user_id = ? and comment_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setLong(1, user.getId());
                preparedStatement.setLong(2, comment.getId());
                preparedStatement.executeUpdate();
            }
            else {
                //removes comment from liked comments
                if(commentIsAlreadyLiked(user, comment)){
                    String unlike = "delete from youtube.users_liked_comments where user_id = ? and comment_id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(unlike);
                    preparedStatement.setLong(1, user.getId());
                    preparedStatement.setLong(2, comment.getId());
                    preparedStatement.executeUpdate();
                }
                //adds comment to disliked comments
                String dislike = "insert into youtube.users_disliked_comments values (? , ?);";
                PreparedStatement preparedStatement = connection.prepareStatement(dislike);
                preparedStatement.setLong(1, user.getId());
                preparedStatement.setLong(2, comment.getId());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new CommentException("Could not dislike comment. Please, try again later.",e);
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
