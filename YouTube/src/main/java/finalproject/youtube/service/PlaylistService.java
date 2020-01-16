package finalproject.youtube.service;

import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.exceptions.BadRequestException;
import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.dao.PlaylistDAO;
import finalproject.youtube.model.dto.ResponsePlaylistDto;
import finalproject.youtube.model.dto.SmallPlaylistDto;
import finalproject.youtube.model.dto.VideoInPlaylistDto;
import finalproject.youtube.model.pojo.Playlist;
import finalproject.youtube.model.pojo.User;
import finalproject.youtube.model.pojo.Video;
import finalproject.youtube.model.repository.PlaylistRepository;
import finalproject.youtube.utils.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class PlaylistService {

    private static final int PLAYLISTS_PER_PAGE = 10;
    @Autowired
    PlaylistRepository playlistRepository;
    @Autowired
    PlaylistDAO        playlistDAO;
    @Autowired
    VideoService       videoService;

    public ResponsePlaylistDto getPlaylistById(long playlistId, int page) {
        //validate page
        Validator.validatePage(page);
        //validate playlist
        Playlist playlist = validateAndGetPlaylist(playlistId);
        //return playlist with all videos in it
        List <VideoInPlaylistDto> videos = playlistDAO.getTenVideosPerPageFromPlaylist(playlist, page-1);
        return new ResponsePlaylistDto(playlist, videos);
    }

    public List<SmallPlaylistDto> getPlaylistsByTitle(String title, int page){
        //validate page
        Validator.validatePage(page);
        //check if there are playlists like this
        List <Playlist> playlists =
                playlistRepository.findAllByTitleContaining(title, PageRequest.of(page-1, PLAYLISTS_PER_PAGE));
        //checks for playlist existence
        if(playlists.isEmpty()){
            throw new NotFoundException("Playlists with title like "+title+" not found!");
        }
        List<SmallPlaylistDto> smallPlaylistDtos = new LinkedList <>();
        //load responses
        for (Playlist p: playlists) {
            smallPlaylistDtos.add(p.toSmallDto());
        }
        return smallPlaylistDtos;
    }

    public ResponsePlaylistDto createPlaylist(User user, String title){
        //validate title
        title = Validator.validateText(title);
        //check if there's already a playlist with this name by this owner
        if(playlistRepository.existsPlaylistByOwnerIdAndTitle(user.getId(), title)){
            throw new BadRequestException("There is already a playlist with this name!");
        }
        //create playlist
        Playlist playlist = new Playlist(title);
        playlist.setOwner(user);
        playlistRepository.save(playlist);

        return new ResponsePlaylistDto(playlist);
    }

    public ResponsePlaylistDto addVideoToPlaylist(User user, long playlistId, long videoId){
        //validate playlist
        Playlist playlist = validateAndGetPlaylist(playlistId);
        //check if video exists
        Video video = videoService.validateAndGetVideo(videoId);
        //check if you're the owner of the playlist
        if(playlist.getOwner().getId() != user.getId()){
            throw new AuthorizationException("You are not the owner of this playlist!");
        }
        //check if the video is already in the playlist
        if(playlistDAO.isVideoInPlaylist(video, playlist)){
            throw new BadRequestException("This video is already in this playlist!");
        }
        //add video to playlist
        playlistDAO.addVideoToPlaylist(video, playlist);
        List<VideoInPlaylistDto> videos = playlistDAO.getAllVideosFromPlaylist(playlist);
        return new ResponsePlaylistDto(playlist, videos);
    }

    public ResponsePlaylistDto removeVideoFromPlaylist(User user, long playlistId, long videoId){
        //validate playlist
        Playlist playlist = validateAndGetPlaylist(playlistId);
        //check if video exists
        Video video = videoService.validateAndGetVideo(videoId);
        //check if you're the owner of the playlist
        if(playlist.getOwner().getId() != user.getId()){
            throw new AuthorizationException("You are not the owner of this playlist!");
        }
        //check if the video is already in the playlist
        if(!playlistDAO.isVideoInPlaylist(video, playlist)){
            throw new BadRequestException("The video is not in this playlist!");
        }
        //remove video from playlist
        playlistDAO.removeVideoFromPlaylist(video, playlist);
        List<VideoInPlaylistDto> videos = playlistDAO.getAllVideosFromPlaylist(playlist);
        return new ResponsePlaylistDto(playlist,videos);
    }

    public void editPlaylistName(User user, long playlistId, String title){
        //validate title
        title = Validator.validateText(title);
        //validate playlist
        Playlist playlist = validateAndGetPlaylist(playlistId);
        //check if you're the owner of the playlist
        if(playlist.getOwner().getId() != user.getId()){
            throw new AuthorizationException("You are not the owner of this playlist!");
        }
        //check if the name is the same
        if(title.equals(playlist.getTitle())){
            throw new BadRequestException("You haven't made any changes to the title!");
        }
        //change name
        playlist.setTitle(title);
        playlistRepository.save(playlist);

    }

    public void deletePlaylist(User user, long playlistId){
        //validate playlist
        Playlist playlist = validateAndGetPlaylist(playlistId);
        //check if you're the owner of the playlist
        if(playlist.getOwner().getId() != user.getId()){
            throw new AuthorizationException("You are not the owner of this playlist!");
        }
        //delete playlist
        playlistRepository.delete(playlist);
    }

    public Playlist validateAndGetPlaylist(long playlistId){
        //validate playlist
        Validator.validatePlaylistId(playlistId);
        //checks for playlist existence
        Optional <Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        if(!optionalPlaylist.isPresent()){
            throw new NotFoundException("Playlist with id="+playlistId+" not found!");
        }
        return optionalPlaylist.get();
    }
}
