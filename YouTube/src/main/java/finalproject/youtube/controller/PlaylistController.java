package finalproject.youtube.controller;

import finalproject.youtube.model.dao.PlaylistDAO;
import finalproject.youtube.model.repository.PlaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlaylistController extends BaseController {

    @Autowired
    PlaylistRepository playlistRepository;
    @Autowired
    PlaylistDAO playlistDAO;

    
}
