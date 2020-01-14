package finalproject.youtube.model.dao;

import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.dto.SmallUserDto;
import finalproject.youtube.model.dto.VideoInPlaylistDto;
import finalproject.youtube.model.pojo.Playlist;
import finalproject.youtube.model.pojo.Video;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class PlaylistDAO {

    private static final int    MAX_VIDEOS_PER_PAGE  = 10;

    private static final String    ADD_VIDEO                    =
            "INSERT INTO youtube.videos_in_playlist VALUES (?,?, now());";
    private static final String    REMOVE_VIDEO                 =
            "DELETE FROM youtube.videos_in_playlist WHERE video_id = ? AND playlist_id = ?;";
    private static final String IS_VIDEO_IN_PLAYLIST         =
            "SELECT * " +
                    "FROM youtube.videos_in_playlist " +
                    "WHERE video_id = ? AND playlist_id = ?;";
    private static final String GET_ALL_VIDEOS_FROM_PLAYLIST =
            "SELECT " +
                    "v.id AS video_id, v.title, v.video_url, v.thumbnail_url, " +
                    "u.id as user_id, u.user_name, " +
                    "vp.time_added " +
                    "FROM youtube.videos AS v " +
                    "JOIN youtube.videos_in_playlist AS vp " +
                    "ON v.id = vp.video_id " +
                    "JOIN youtube.playlists AS p " +
                    "ON p.id = vp.playlist_id " +
                    "JOIN youtube.users AS u " +
                    "ON v.owner_id=u.id " +
                    "WHERE vp.playlist_id = ?;"
            ;
    private static       String PAGINATED_PLAYLIST           =
            "SELECT " +
                    "v.id AS video_id, v.title, v.video_url, v.thumbnail_url, " +
                    "u.id as user_id, u.user_name, " +
                    "vp.time_added " +
                    "FROM youtube.videos AS v " +
                    "JOIN youtube.videos_in_playlist AS vp " +
                    "ON v.id = vp.video_id " +
                    "JOIN youtube.playlists AS p " +
                    "ON p.id = vp.playlist_id " +
                    "JOIN youtube.users AS u " +
                    "ON v.owner_id=u.id " +
                    "WHERE vp.playlist_id = ? " +
                    "limit ? offset ?;";


    @Autowired
    JdbcTemplate jdbcTemplate;

    private PlaylistDAO() {
    }

   @SneakyThrows
    public void addVideoToPlaylist(Video video, Playlist playlist){
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(ADD_VIDEO);
            preparedStatement.setLong(1, video.getId());
            preparedStatement.setLong(2, playlist.getId());
            preparedStatement.executeUpdate();
    }

    @SneakyThrows
    public void removeVideoFromPlaylist(Video video, Playlist playlist){
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(REMOVE_VIDEO);
            preparedStatement.setLong(1, video.getId());
            preparedStatement.setLong(2, playlist.getId());
            preparedStatement.executeUpdate();
    }

    @SneakyThrows
    public List<VideoInPlaylistDto> getAllVideosFromPlaylist(Playlist playlist) {
        List <VideoInPlaylistDto> videos = new ArrayList <>();
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(GET_ALL_VIDEOS_FROM_PLAYLIST);
        preparedStatement.setLong(1, playlist.getId());
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            VideoInPlaylistDto video = new VideoInPlaylistDto(
                    resultSet.getInt("video_id"),
                    resultSet.getString("title"),
                    resultSet.getString("video_url"),
                    resultSet.getString("thumbnail_url"),
                    new SmallUserDto(resultSet.getLong("user_id"),
                            resultSet.getString("user_name")),
                    resultSet.getTimestamp("time_added").toLocalDateTime()
            );

            videos.add(video);
        }
        return videos;
    }

    @SneakyThrows
    public boolean isVideoInPlaylist(Video video, Playlist playlist) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(IS_VIDEO_IN_PLAYLIST)) {
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

    @SneakyThrows
    public List<VideoInPlaylistDto> getTenVideosPerPageFromPlaylist(Playlist playlist, int page) {
        List <VideoInPlaylistDto> videos = new ArrayList <>();
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(PAGINATED_PLAYLIST);
        preparedStatement.setLong(1, playlist.getId());
        preparedStatement.setInt(2, MAX_VIDEOS_PER_PAGE);
        preparedStatement.setInt(3, page*MAX_VIDEOS_PER_PAGE);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (!resultSet.next()){
            throw new NotFoundException("There are no videos on this page");
        }
        do{
            VideoInPlaylistDto video = new VideoInPlaylistDto(
                    resultSet.getInt("video_id"),
                    resultSet.getString("title"),
                    resultSet.getString("video_url"),
                    resultSet.getString("thumbnail_url"),
                    new SmallUserDto(resultSet.getLong("user_id"),
                            resultSet.getString("user_name")),
                    resultSet.getTimestamp("time_added").toLocalDateTime()
            );
            videos.add(video);
        }while (resultSet.next());
        return videos;
    }
}
