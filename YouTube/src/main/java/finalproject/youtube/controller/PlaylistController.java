package finalproject.youtube.controller;

import finalproject.youtube.model.dto.SmallPlaylistDto;
import finalproject.youtube.utils.SessionManager;
import finalproject.youtube.model.dto.ResponsePlaylistDto;
import finalproject.youtube.model.pojo.User;
import finalproject.youtube.service.PlaylistService;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@RestController
@Validated
public class PlaylistController extends BaseController {
    @Autowired
    PlaylistService playlistService;

    @SneakyThrows
    @GetMapping(value = "/playlists/{playlistId}")
    public ResponseEntity<ResponsePlaylistDto> getPlaylistById(
            @PathVariable("playlistId") long playlistId,
            @RequestParam(value = "page", defaultValue = "1") int page){
        return new ResponseEntity <>( playlistService.getPlaylistById(playlistId, page), HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping(value = "/playlists/title")
    public ResponseEntity<List<SmallPlaylistDto>> getPlaylistsByTitle(
            @RequestParam(value = "title", defaultValue = "") String title,
            @RequestParam(value = "page", defaultValue = "1") int page){
        return new ResponseEntity <>( playlistService.getPlaylistsByTitle(title, page), HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping(value = "/playlists")
    public ResponseEntity<ResponsePlaylistDto> createPlaylist(HttpSession session,
                                                              @RequestParam(value = "title",
                                                                      defaultValue = "") String title){
        User currentUser = SessionManager.getLoggedUser(session);
        return new ResponseEntity<>(playlistService.createPlaylist(currentUser, title),HttpStatus.OK);
    }

    //todo validate!!! long not string
    @SneakyThrows
    @PostMapping(value = "/playlists/{playlistId}/add")
    public ResponseEntity<ResponsePlaylistDto> addVideoToPlaylist(HttpSession session,
                                                                  @PathVariable("playlistId") long playlistId,
                                                                  @RequestParam("videoId") @Valid long videoId){
        User user = SessionManager.getLoggedUser(session);
        return new ResponseEntity <>(playlistService.addVideoToPlaylist(user,playlistId, videoId), HttpStatus.OK);
    }

    @SneakyThrows
    @DeleteMapping(value = "/playlists/{playlistId}")
    public ResponseEntity<String> deletePlaylist(HttpSession session,
                                                 @PathVariable("playlistId") long playlistId){
        User user = SessionManager.getLoggedUser(session);
        playlistService.deletePlaylist(user, playlistId);
        return new ResponseEntity <>("Playlist with id="+playlistId+" deleted!", HttpStatus.OK);
    }

    //todo validate!!! long not string
    @SneakyThrows
    @DeleteMapping("/playlists/{playlistId}/remove")
    public ResponseEntity<ResponsePlaylistDto> removeVideoFromPlaylist(HttpSession session,
                                                           @PathVariable("playlistId") long playlistId,
                                                           @RequestParam("videoId") @Valid long videoId){
        User user = SessionManager.getLoggedUser(session);
        return new ResponseEntity <>(playlistService.removeVideoFromPlaylist(user, playlistId, videoId),HttpStatus.OK);
    }

    @SneakyThrows
    @PutMapping(value = "/playlists/{playlistId}")
    public ResponseEntity <String> editPlaylistName(HttpSession session,
                                                    @PathVariable("playlistId") long playlistId,
                                                    @RequestParam(value = "title", defaultValue = "") String title){
        User user = SessionManager.getLoggedUser(session);
        playlistService.editPlaylistName(user, playlistId, title);
        return new ResponseEntity <>("Playlist name changed successfully!", HttpStatus.OK);
    }
}
