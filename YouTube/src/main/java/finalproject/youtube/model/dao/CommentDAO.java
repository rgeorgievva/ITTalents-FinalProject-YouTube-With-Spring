package finalproject.youtube.model.dao;

import finalproject.youtube.model.pojo.Comment;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
public class CommentDAO {

    public static final  int    DISLIKE             = -1;
    public static final  int    LIKE                = 1;
    private static final String HAS_IT_BEEN_REACTED =
            "SELECT * FROM comments_reactions WHERE user_id = ? AND comment_id = ?";
    private static final String REACT_TO_COMMENT    =
            "INSERT INTO comments_reactions (user_id, comment_id, reaction) VALUES (?, ?, ?);";
    private static final String ADD_LIKE            =
            "UPDATE comments SET likes = ? WHERE id = ?;";
    private static final String ADD_DISLIKE         =
            "UPDATE comments SET dislikes = ? WHERE id = ?;";
    public static final  String CHANGE_REACTION     =
            "UPDATE comments_reactions SET reaction = ? WHERE user_id = ? AND comment_id = ?;";
    public static final  String UPDATE_REACTIONS =
            "UPDATE comments SET likes = ?, dislikes = ? WHERE id = ?;";
    private static final String REMOVE_REACTION   =
            "DELETE FROM comments_reactions WHERE user_id = ? AND comment_id = ?;";

    @Autowired
    JdbcTemplate jdbcTemplate;

    private CommentDAO(){}

    //liking and disliking a comment, based on reaction int
    @SneakyThrows
    public void react(long userId, Comment comment, int reaction) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(HAS_IT_BEEN_REACTED)){
            statement.setLong(1, userId);
            statement.setLong(2, comment.getId());
            ResultSet resultSet = statement.executeQuery();
            //no reaction
            if (!resultSet.next()) {
                reactToComment(userId, comment, reaction);
            }
            else{
                int currentReaction = resultSet.getInt("reaction");
                if (currentReaction != reaction) {
                    //change reaction -> delete previous and set new reaction
                    changeReaction(userId, comment, reaction);
                }
                else {
                    //remove previous reaction
                    removeReaction(userId, comment, reaction);
                }
            }
        }
    }

    //adding like or dislike to comment, and data for how each user reacted to comment
    @SneakyThrows
    private void reactToComment(long userId, Comment comment, int reaction) {
        String changeNumberOfReactions;
        int reactionsCount;
        //check if the reaction is a like
        if (reaction == LIKE) {
            changeNumberOfReactions = ADD_LIKE;
            reactionsCount = comment.getLikes() + 1;
        }
        //the reaction is dislike
        else{
            changeNumberOfReactions = ADD_DISLIKE;
            reactionsCount = comment.getDislikes() + 1;
        }

        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            try (PreparedStatement reactToCommentStatement = connection.prepareStatement(REACT_TO_COMMENT);
                 PreparedStatement changeNumberReactionsStatement = connection.prepareStatement(changeNumberOfReactions)) {
                connection.setAutoCommit(false);
                reactToCommentStatement.setLong(1, userId);
                reactToCommentStatement.setLong(2, comment.getId());
                reactToCommentStatement.setInt(3, reaction);
                reactToCommentStatement.executeUpdate();

                changeNumberReactionsStatement.setInt(1, reactionsCount);
                changeNumberReactionsStatement.setLong(2, comment.getId());
                changeNumberReactionsStatement.executeUpdate();

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

    //removing comment reaction if the user has already reacted this way
    @SneakyThrows
    private void removeReaction(long userId, Comment comment, int reaction) {
        String sql;
        int updatedNumber;
        if (reaction == LIKE) {
            sql = ADD_LIKE;
            updatedNumber = comment.getLikes() - 1;
        }
        else {
            sql = ADD_DISLIKE;
            updatedNumber = comment.getDislikes() - 1;
        }
        try(Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            try (PreparedStatement statement1 = connection.prepareStatement(REMOVE_REACTION);
                 PreparedStatement statement2 = connection.prepareStatement(sql);
            ) {
                connection.setAutoCommit(false);
                statement1.setLong(1, userId);
                statement1.setLong(2, comment.getId());
                statement1.executeUpdate();

                statement2.setInt(1, updatedNumber);
                statement2.setLong(2, comment.getId());
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

    //replacing old reaction with the other reaction
    @SneakyThrows
    private void changeReaction(long userId, Comment comment, int reaction) {
        int numberOfLikes;
        int numberOfDislikes;
        if (reaction == LIKE) {
            numberOfLikes = comment.getLikes() + 1;
            numberOfDislikes = comment.getDislikes() - 1;
        }
        else {
            numberOfLikes = comment.getLikes() - 1;
            numberOfDislikes = comment.getDislikes() + 1;
        }
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        try (PreparedStatement changeReactionStatement = connection.prepareStatement(CHANGE_REACTION);
             PreparedStatement updateReactionsStatement = connection.prepareStatement(UPDATE_REACTIONS)
        ) {
            connection.setAutoCommit(false);
            changeReactionStatement.setInt(1, reaction);
            changeReactionStatement.setLong(2, userId);
            changeReactionStatement.setLong(3, comment.getId());
            changeReactionStatement.executeUpdate();

            updateReactionsStatement.setInt(1, numberOfLikes);
            updateReactionsStatement.setInt(2, numberOfDislikes);
            updateReactionsStatement.setLong(3, comment.getId());
            updateReactionsStatement.executeUpdate();
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
