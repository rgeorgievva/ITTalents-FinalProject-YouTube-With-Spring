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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
        Optional<Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        if(!optionalPlaylist.isPresent()){
            throw new NotFoundException("Playlist with id="+playlistId+" not found!");
        }
        //return playlist with all videos in it
        Playlist playlist = optionalPlaylist.get();
        List <Video> videos = playlistDAO.getAllVideosFromPlaylist(playlist);
        return new ResponseEntity <>( new ResponsePlaylistDto(playlist, videos), HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping(value = "/playlists/byTitle/{playlist_title}")
    public ResponseEntity<List<ResponsePlaylistDto>> getPlaylistsByTitle(@PathVariable("playlist_title") String title){
        Optional<List<Playlist>> optionalPlaylists = playlistRepository.findAllByTitleContaining(title);
        //checks for playlist existence
        if(!optionalPlaylists.isPresent()){
            throw new NotFoundException("Playlists with title like "+title+" not found!");
        }
        //return a list of playlists
        List<Playlist> playlists = optionalPlaylists.get();
        // get all playlists with all videos in them
        List<ResponsePlaylistDto> responseDTOs = new LinkedList <>();
        //load response dto
        for (Playlist p: playlists) {
            List <Video> videos = playlistDAO.getAllVideosFromPlaylist(p);
            responseDTOs.add(new ResponsePlaylistDto(p,videos));
        }
        return new ResponseEntity <>( responseDTOs, HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping(value = "/playlists")
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

        return new ResponseEntity<>(new ResponsePlaylistDto(playlist, null),HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping(value = "/playlists/{playlist_id}/add/{video_id}")
    public ResponseEntity<ResponsePlaylistDto> addVideoToPlaylist(HttpSession session,
                                                                  @PathVariable("playlist_id") long playlistId,
                                                                  @PathVariable("video_id") long videoId){

        //checks for playlist existence
        Optional<Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        if(!optionalPlaylist.isPresent()){
            throw new NotFoundException("Playlist with id="+playlistId+" not found!");
        }
        //check if video exists
        Optional<Video> optionalVideo = videoRepository.findById(videoId);
        if(!optionalVideo.isPresent()){
            throw new NotFoundException("Video not found!");
        }
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to add video to playlist!");
        }
        //check if you're the owner of the playlist
        Playlist playlist = optionalPlaylist.get();
        if(playlist.getOwner().getId() != SessionManager.getLoggedUser(session).getId()){
            throw new AuthorizationException("You are not the owner of this playlist!");
        }
        //check if the video is already in the playlist
        Video video = optionalVideo.get();
        if(playlistDAO.isVideoInPlaylist(video, playlist)){
            throw new BadRequestException("This video is already in this playlist!");
        }
        //add video to playlist
        playlistDAO.addVideoToPlaylist(video, playlist);
        List<Video> videos = playlistDAO.getAllVideosFromPlaylist(playlist);
        ResponsePlaylistDto responsePlaylistDto = new ResponsePlaylistDto(playlist, videos);

        return new ResponseEntity <>(responsePlaylistDto, HttpStatus.OK);
    }
    @SneakyThrows
    @DeleteMapping(value = "/playlists/{playlist_id}")
    public ResponseEntity<String> deletePlaylist(HttpSession session,
                                                 @PathVariable("playlist_id") long playlistId){
        //checks for playlist existence
        Optional<Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        if(!optionalPlaylist.isPresent()){
            throw new NotFoundException("Playlist with id="+playlistId+" not found!");
        }
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to delete playlist!");
        }
        //check if you're the owner of the playlist
        Playlist playlist = optionalPlaylist.get();
        if(playlist.getOwner().getId() != SessionManager.getLoggedUser(session).getId()){
            throw new AuthorizationException("You are not the owner of this playlist!");
        }
        //delete playlist
        playlistRepository.delete(playlist);

        return new ResponseEntity <>("Playlist with id="+playlistId+" deleted!", HttpStatus.OK);
    }

    @SneakyThrows
    @DeleteMapping("/playlists/{playlist_id}/remove/{video_id}")
    public ResponseEntity<ResponsePlaylistDto> removeVideoFromPlaylist(HttpSession session,
                                                           @PathVariable("playlist_id") long playlistId,
                                                           @PathVariable("video_id") long videoId){
        //checks for playlist existence
        Optional<Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        if(!optionalPlaylist.isPresent()){
            throw new NotFoundException("Playlist with id="+playlistId+" not found!");
        }
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to remove a video from the playlist!");
        }
        //check if video exists
        Optional<Video> optionalVideo = videoRepository.findById(videoId);
        if(!optionalVideo.isPresent()){
            throw new NotFoundException("Video with id "+videoId+" not found!");
        }
        //check if you're the owner of the playlist
        Playlist playlist = optionalPlaylist.get();
        if(playlist.getOwner().getId() != SessionManager.getLoggedUser(session).getId()){
            throw new AuthorizationException("You are not the owner of this playlist!");
        }
        //check if the video is already in the playlist
        Video video = optionalVideo.get();
        if(!playlistDAO.isVideoInPlaylist(video, playlist)){
            throw new BadRequestException("The video is not in this playlist!");
        }
        //remove video from playlist
        playlistDAO.removeVideoFromPlaylist(video, playlist);
        List<Video> videos = playlistDAO.getAllVideosFromPlaylist(playlist);

        return new ResponseEntity <>(new ResponsePlaylistDto(playlist,videos),HttpStatus.OK);
    }

    @SneakyThrows
    @PutMapping(value = "/playlists/{playlist_id}")
    public ResponseEntity <String> editPlaylistName(HttpSession session,
                                                    @PathVariable("playlist_id") long playlistId,
                                                    @RequestBody RequestPlaylistDto playlistDto){
        //checks for playlist existence
        Optional<Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        if(!optionalPlaylist.isPresent()){
            throw new NotFoundException("Playlist with id="+playlistId+" not found!");
        }
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to edit playlist!");
        }
        //check if you're the owner of the playlist
        Playlist playlist = optionalPlaylist.get();
        if(playlist.getOwner().getId() != SessionManager.getLoggedUser(session).getId()){
            throw new AuthorizationException("You are not the owner of this playlist!");
        }
        //change name
        playlist.setTitle(playlistDto.getTitle());
        playlistRepository.save(playlist);

        return new ResponseEntity <>("Playlist name changed successfully!", HttpStatus.OK);
    }
}
