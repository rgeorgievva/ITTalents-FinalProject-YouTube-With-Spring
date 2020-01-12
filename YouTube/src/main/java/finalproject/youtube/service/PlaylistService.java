package finalproject.youtube.service;

import finalproject.youtube.exceptions.AuthorizationException;
import finalproject.youtube.exceptions.BadRequestException;
import finalproject.youtube.exceptions.NotFoundException;
import finalproject.youtube.model.dao.PlaylistDAO;
import finalproject.youtube.model.dto.RequestPlaylistDto;
import finalproject.youtube.model.dto.ResponsePlaylistDto;
import finalproject.youtube.model.dto.VideoInPlaylistDto;
import finalproject.youtube.model.pojo.Playlist;
import finalproject.youtube.model.pojo.User;
import finalproject.youtube.model.pojo.Video;
import finalproject.youtube.model.repository.PlaylistRepository;
import finalproject.youtube.model.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class PlaylistService {


    @Autowired
    PlaylistRepository playlistRepository;
    @Autowired
    PlaylistDAO        playlistDAO;
    @Autowired
    VideoService       videoService;

    public ResponsePlaylistDto getPlaylistById( long playlistId){
        //checks for playlist existence
        Optional <Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        if(!optionalPlaylist.isPresent()){
            throw new NotFoundException("Playlist with id="+playlistId+" not found!");
        }
        //return playlist with all videos in it
        Playlist playlist = optionalPlaylist.get();
        List <VideoInPlaylistDto> videos = playlistDAO.getAllVideosFromPlaylist(playlist);
        return new ResponsePlaylistDto(playlist, videos);
    }

    public ResponsePlaylistDto getTenFromPlaylistById(long playlistId, int page) {
        //checks for playlist existence
        Optional <Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        if(!optionalPlaylist.isPresent()){
            throw new NotFoundException("Playlist with id="+playlistId+" not found!");
        }
        //return playlist with all videos in it
        Playlist playlist = optionalPlaylist.get();
        List <VideoInPlaylistDto> videos = playlistDAO.getTenVideosPerPageFromPlaylist(playlist, page);
        return new ResponsePlaylistDto(playlist, videos);
    }

    public List<ResponsePlaylistDto> getPlaylistsByTitle(String title){
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
            List <VideoInPlaylistDto> videos = playlistDAO.getAllVideosFromPlaylist(p);
            responseDTOs.add(new ResponsePlaylistDto(p,videos));
        }
        return responseDTOs;
    }

    public ResponsePlaylistDto createPlaylist(User user, RequestPlaylistDto requestPlaylist){
        if(playlistRepository.existsPlaylistByOwnerIdAndTitle(user.getId(), requestPlaylist.getTitle())){
            throw new BadRequestException("There is already a playlist with this name!");
        }
        //create playlist
        Playlist playlist = new Playlist(requestPlaylist);
        playlist.setOwner(user);
        playlistRepository.save(playlist);

        return new ResponsePlaylistDto(playlist);
    }

    public ResponsePlaylistDto addVideoToPlaylist(User user, long playlistId, long videoId){
        //checks for playlist existence
        Optional<Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        if(!optionalPlaylist.isPresent()){
            throw new NotFoundException("Playlist with id="+playlistId+" not found!");
        }
        //check if video exists
        Video video = videoService.validateAndGetVideo(videoId);

        //check if you're the owner of the playlist
        Playlist playlist = optionalPlaylist.get();
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
        //checks for playlist existence
        Optional<Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        if(!optionalPlaylist.isPresent()){
            throw new NotFoundException("Playlist with id="+playlistId+" not found!");
        }
        //check if video exists
        Video video = videoService.validateAndGetVideo(videoId);
        //check if you're the owner of the playlist
        Playlist playlist = optionalPlaylist.get();
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
        //checks for playlist existence
        Optional<Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        if(!optionalPlaylist.isPresent()){
            throw new NotFoundException("Playlist with id="+playlistId+" not found!");
        }

        //check if you're the owner of the playlist
        Playlist playlist = optionalPlaylist.get();
        if(playlist.getOwner().getId() != user.getId()){
            throw new AuthorizationException("You are not the owner of this playlist!");
        }
        //change name
        playlist.setTitle(title);
        playlistRepository.save(playlist);

    }

    public void deletePlaylist(User user, long playlistId){
        //checks for playlist existence
        Optional<Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        if(!optionalPlaylist.isPresent()){
            throw new NotFoundException("Playlist with id="+playlistId+" not found!");
        }

        //check if you're the owner of the playlist
        Playlist playlist = optionalPlaylist.get();
        if(playlist.getOwner().getId() != user.getId()){
            throw new AuthorizationException("You are not the owner of this playlist!");
        }
        //delete playlist
        playlistRepository.delete(playlist);
    }
}
