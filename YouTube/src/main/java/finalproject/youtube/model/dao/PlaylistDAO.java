package finalproject.youtube.model.dao;

import finalproject.youtube.model.entity.Playlist;
import finalproject.youtube.model.entity.Video;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class PlaylistDAO {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private PlaylistDAO() {
    }

   @SneakyThrows
    public void addVideoToPlaylist(Video video, Playlist playlist){
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            String sql = "insert into youtube.videos_in_playlist values (?,?, now());";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, video.getId());
            preparedStatement.setLong(2, playlist.getId());
            preparedStatement.executeUpdate();
    }

    @SneakyThrows
    public void removeVideoFromPlaylist(Video video, Playlist playlist){
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            String sql = "delete from youtube.videos_in_playlist where video_id = ? and playlist_id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, video.getId());
            preparedStatement.setLong(2, playlist.getId());
            preparedStatement.executeUpdate();
    }

    @SneakyThrows
    public List<Video> getAllVideosFromPlaylist(Playlist playlist) {
        List <Video> videos = new ArrayList <>();
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        String sql = "select *\n" +
                "from youtube.videos as v\n" +
                "join youtube.videos_in_playlist as vp\n" +
                "on v.id = vp.video_id\n" +
                "where vp.playlist_id = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setLong(1, playlist.getId());
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Video video = new Video(resultSet.getInt("id"),
                    resultSet.getString("title"),
                    resultSet.getString("description"),
                    resultSet.getString("video_url"),
                    resultSet.getString("thumbnail_url"),
                    resultSet.getTimestamp("date_uploaded").toLocalDateTime(),
                    resultSet.getInt("owner_id"),
                    resultSet.getInt("category_id"),
                    resultSet.getString("status")
            );

            videos.add(video);
        }
        return videos;
    }

    @SneakyThrows
    public boolean isVideoInPlaylist(Video video, Playlist playlist) {
        String sql = "select * from youtube.videos_in_playlist where video_id = ? and playlist_id = ?;";
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, video.getId());
                preparedStatement.setLong(2, playlist.getId());
                ResultSet result = preparedStatement.executeQuery();
                while (result.next()) {
                   /* Video videoFromPlaylist = new Video(result.getLong("id"),
                            result.getString("title"),
                            result.getString("description"),
                            result.getString("video_url"),
                            result.getString("thumbnail_url"),
                            result.getTimestamp("date_uploaded").toLocalDateTime(),
                            result.getLong("owner_id"),
                            result.getLong("category_id"),
                            result.getString("status")
                    );*/
                   long currentVideoInPlaylistID = result.getLong("video_id");
                    if(currentVideoInPlaylistID == video.getId()){
                        return true;
                    }
                }
                return false;
            }
        }
    }
}
