package finalproject.youtube.controller;

import finalproject.youtube.utils.SessionManager;
import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.model.dto.RequestPlaylistDto;
import finalproject.youtube.model.dto.ResponsePlaylistDto;
import finalproject.youtube.model.pojo.User;
import finalproject.youtube.service.PlaylistService;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@RestController
@Valid
public class PlaylistController extends BaseController {
    @Autowired
    PlaylistService playlistService;

    @SneakyThrows
    @GetMapping(value = "/playlists/{playlist_id}")
    public ResponseEntity<ResponsePlaylistDto> getPlaylistById(@PathVariable("playlist_id") long playlistId){
        return new ResponseEntity <>( playlistService.getPlaylistById(playlistId), HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping(value = "/playlists/{playlist_id}/page")
    public ResponseEntity<ResponsePlaylistDto> getTenVideosPerPagePlaylistById(
            @PathVariable("playlist_id") long playlistId,
            @RequestParam("page_num") int page){
        return new ResponseEntity <>( playlistService.getTenFromPlaylistById(playlistId, page-1), HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping(value = "/playlists/byTitle/{playlist_title}")
    public ResponseEntity<List<ResponsePlaylistDto>> getPlaylistsByTitle(@PathVariable("playlist_title") String title){
        return new ResponseEntity <>( playlistService.getPlaylistsByTitle(title), HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping(value = "/playlists")
    public ResponseEntity<ResponsePlaylistDto> createPlaylist(HttpSession session,
                                                              @RequestBody @Valid RequestPlaylistDto requestPlaylist){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to create a playlist!");
        }
        User currentUser = SessionManager.getLoggedUser(session);
        return new ResponseEntity<>(playlistService.createPlaylist(currentUser, requestPlaylist),HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping(value = "/playlists/{playlist_id}/add/{video_id}")
    public ResponseEntity<ResponsePlaylistDto> addVideoToPlaylist(HttpSession session,
                                                                  @PathVariable("playlist_id") long playlistId,
                                                                  @PathVariable("video_id") long videoId){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to add video to playlist!");
        }
        User user = SessionManager.getLoggedUser(session);
        return new ResponseEntity <>(playlistService.addVideoToPlaylist(user,playlistId, videoId), HttpStatus.OK);
    }

    @SneakyThrows
    @DeleteMapping(value = "/playlists/{playlist_id}")
    public ResponseEntity<String> deletePlaylist(HttpSession session,
                                                 @PathVariable("playlist_id") long playlistId){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to delete playlist!");
        }
        User user = SessionManager.getLoggedUser(session);
        playlistService.deletePlaylist(user, playlistId);
        return new ResponseEntity <>("Playlist with id="+playlistId+" deleted!", HttpStatus.OK);
    }

    @SneakyThrows
    @DeleteMapping("/playlists/{playlist_id}/remove/{video_id}")
    public ResponseEntity<ResponsePlaylistDto> removeVideoFromPlaylist(HttpSession session,
                                                           @PathVariable("playlist_id") long playlistId,
                                                           @PathVariable("video_id") long videoId){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to remove a video from the playlist!");
        }
        User user = SessionManager.getLoggedUser(session);
        return new ResponseEntity <>(playlistService.removeVideoFromPlaylist(user, playlistId, videoId),HttpStatus.OK);
    }

    @SneakyThrows
    @PutMapping(value = "/playlists/{playlist_id}")
    public ResponseEntity <String> editPlaylistName(HttpSession session,
                                                    @PathVariable("playlist_id") long playlistId,
                                                    @RequestBody @Valid RequestPlaylistDto playlistDto){
        //checks for being logged in
        if (!SessionManager.validateLogged(session)) {
            throw new AuthorizationException("Please login to edit playlist!");
        }
        User user = SessionManager.getLoggedUser(session);
        String title = playlistDto.getTitle();
        playlistService.editPlaylistName(user, playlistId, title);
        return new ResponseEntity <>("Playlist name changed successfully!", HttpStatus.OK);
    }
}
