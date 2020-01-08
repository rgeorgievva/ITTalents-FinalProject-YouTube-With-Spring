package finalproject.youtube.controller;

import finalproject.youtube.model.dao.PlaylistDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlaylistController {

    @Autowired
    PlaylistDAO playlistDAO;
}
