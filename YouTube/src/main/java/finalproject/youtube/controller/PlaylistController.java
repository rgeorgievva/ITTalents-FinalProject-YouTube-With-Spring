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
import finalproject.youtube.model.repository.PlaylistRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
public class PlaylistController extends BaseController {

    @Autowired
    PlaylistRepository playlistRepository;
    @Autowired
    PlaylistDAO playlistDAO;


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

}
