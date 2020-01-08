package finalproject.youtube.controller;

import finalproject.youtube.SessionManager;
import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.exceptions.BadRequestException;
import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.dao.PlaylistDAO;
import finalproject.youtube.model.dto.RequestPlaylistDto;
import finalproject.youtube.model.dto.ResponsePlaylistDto;
import finalproject.youtube.model.entity.Playlist;
import finalproject.youtube.model.entity.User;
import finalproject.youtube.model.entity.Video;
import finalproject.youtube.model.repository.PlaylistRepository;
import finalproject.youtube.model.repository.VideoRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
public class PlaylistController extends BaseController {

    @Autowired
    PlaylistRepository playlistRepository;
    @Autowired
    PlaylistDAO playlistDAO;
    @Autowired
    VideoRepository videoRepository;


    @SneakyThrows
    @GetMapping(value = "/playlists/{playlist_id}")
    public ResponseEntity<ResponsePlaylistDto> getPlaylistById(@PathVariable("playlist_id") long playlistId){
        //checks for playlist existence
        if(!playlistRepository.existsPlaylistById(playlistId)){
            throw new NotFoundException("Playlist with id="+playlistId+" not found!");
        }
        //return playlist with all videos in it
        Playlist playlist = playlistRepository.getPlaylistById(playlistId);
        List <Video> videos = playlistDAO.getAllVideosFromPlaylist(playlist);
        return new ResponseEntity <>( new ResponsePlaylistDto(playlist, videos), HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping(value = "/playlists/create")
    public ResponseEntity<ResponsePlaylistDto> createPlaylist(HttpSession session,
                                                              @RequestBody RequestPlaylistDto requestPlaylist){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to create a playlist!");
        }
        //check if the user already has a playlist with the same title
        User currentUser = SessionManager.getLoggedUser(session);
        if(playlistRepository.existsPlaylistByOwnerIdAndTitle(currentUser.getId(), requestPlaylist.getTitle())){
            throw new BadRequestException("There is already a playlist with this name!");
        }
        //create playlist
        Playlist playlist = new Playlist(requestPlaylist);
        playlist.setOwner(currentUser);
        playlistRepository.save(playlist);

        return new ResponseEntity<>(new ResponsePlaylistDto(playlist),HttpStatus.OK);
    }

    //todo test
    @SneakyThrows
    @PostMapping(value = "/playlists/{playlist_id}/add/{video_id}")
    public ResponseEntity<ResponsePlaylistDto> addVideoToPlaylist(HttpSession session,
                                                                  @PathVariable("playlist_id") long playlistId,
                                                                  @PathVariable("video_id") long videoId){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to add videos to playlist!");
        }
        //check if playlist exists
        if(!playlistRepository.existsPlaylistById(playlistId)){
            throw new NotFoundException("Playlist with id="+playlistId+" not found!");
        }
        //check if video exists
        if(!videoRepository.existsVideoById(videoId)){
            throw new NotFoundException("Video with id "+videoId+" not found!");
        }
        //check if you're the owner of the playlist
        Playlist playlist = playlistRepository.getPlaylistById(playlistId);
        if(playlist.getOwner().getId() != SessionManager.getLoggedUser(session).getId()){
            throw new AuthorizationException("You are not the owner of this playlist!");
        }
        //check if the video is already in the playlist
        List<Video> videos = playlistDAO.getAllVideosFromPlaylist(playlist);
        Video video = videoRepository.getVideoById(videoId);
        if(videos.contains(video)){
            throw new BadRequestException("This video is already in this playlist!");
        }
        //add video to playlist
        videos.add(video);
        ResponsePlaylistDto responsePlaylistDto = new ResponsePlaylistDto(playlist, videos);

        return new ResponseEntity <>(responsePlaylistDto, HttpStatus.OK);
    }


    @SneakyThrows
    @DeleteMapping(value = "/playlists/{playlist_id}/delete")
    public ResponseEntity<String> deletePlaylist(HttpSession session,
                                                 @PathVariable("playlist_id") long playlistId){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to delete playlist!");
        }
        //check if playlist exists
        if(!playlistRepository.existsPlaylistById(playlistId)){
            throw new NotFoundException("Playlist with id="+playlistId+" not found!");
        }
        //check if you're the owner of the playlist
        Playlist playlist = playlistRepository.getPlaylistById(playlistId);
        if(playlist.getOwner().getId() != SessionManager.getLoggedUser(session).getId()){
            throw new AuthorizationException("You are not the owner of this playlist!");
        }
        //delete playlist
        playlistRepository.delete(playlist);

        return new ResponseEntity <>("Playlist with id="+playlistId+" deleted!", HttpStatus.OK);
    }



    //todo removeVideoFromPlaylist, editPlaylistName, deletePlaylist, getPlaylistByTitle
}
