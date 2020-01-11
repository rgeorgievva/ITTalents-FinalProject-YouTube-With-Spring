package finalproject.youtube.model.dao;

import finalproject.youtube.model.dto.VideoInPlaylistDto;
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
    public List<VideoInPlaylistDto> getAllVideosFromPlaylist(Playlist playlist) {
        List <VideoInPlaylistDto> videos = new ArrayList <>();
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        String sql = "select \n" +
                "v.id as video_id, v.title, v.video_url, v.thumbnail_url, " +
                "u.user_name, " +
                "vp.time_added\n" +
                "from youtube.videos as v\n" +
                "join youtube.videos_in_playlist as vp\n" +
                "on v.id = vp.video_id\n" +
                "join youtube.playlists as p\n" +
                "on p.id = vp.playlist_id\n" +
                "join youtube.users as u\n" +
                "on v.owner_id=u.id\n" +
                "where vp.playlist_id = ?;\n";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setLong(1, playlist.getId());
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            VideoInPlaylistDto video = new VideoInPlaylistDto(
                    resultSet.getInt("video_id"),
                    resultSet.getString("title"),
                    resultSet.getString("video_url"),
                    resultSet.getString("thumbnail_url"),
                    resultSet.getString("user_name"),
                    resultSet.getTimestamp("time_added").toLocalDateTime()
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
                    if(result.next()){
                        return true;
                    }
                return false;
            }
        }
    }
}
