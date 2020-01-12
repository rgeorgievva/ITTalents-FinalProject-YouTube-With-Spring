package finalproject.youtube.model.pojo;

import finalproject.youtube.model.dto.PendingVideoDto;
import finalproject.youtube.model.dto.ResponseCommentWithRepliesDto;
import finalproject.youtube.model.dto.VideoDto;
import finalproject.youtube.model.dto.VideoWholeInfoDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "videos")
public class Video {

    public enum Status {
        PENDING, UPLOADED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "title")
    private String title;
    @Column(name = "description")
    private String description;
    @Column(name = "video_url")
    private String videoUrl;
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    @Column(name = "date_uploaded")
    private LocalDateTime dateUploaded;
    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id", nullable = false, updatable = false)
    private User owner;
    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false, updatable = false)
    private Category category;
    @Column(name = "status")
    private String  status;
    @Column(name = "number_likes")
    private int numberLikes;
    @Column(name = "number_dislikes")
    private int numberDislikes;

    public VideoDto toVideoDto() {
        VideoDto videoDto = new VideoDto();
        videoDto.setId(this.id);
        videoDto.setTitle(this.title);
        videoDto.setDateUploaded(this.dateUploaded);
        videoDto.setOwner(this.owner.toNoPasswordUserDto());
        return videoDto;
    }

    public PendingVideoDto toPendingVideoDto() {
        PendingVideoDto videoDto = new PendingVideoDto();
        videoDto.setId(this.id);
        videoDto.setTitle(this.title);
        videoDto.setDescription(this.description);
        videoDto.setDateUploaded(this.dateUploaded);
        videoDto.setOwner(this.owner.toNoPasswordUserDto());
        videoDto.setCategory(this.category);
        videoDto.setStatus(this.getStatus());
        return videoDto;
    }

    public VideoWholeInfoDto toVideoWholeInfoDto(List<ResponseCommentWithRepliesDto> comments) {
        VideoWholeInfoDto videoDto = new VideoWholeInfoDto();
        videoDto.setId(this.id);
        videoDto.setTitle(this.title);
        videoDto.setDescription(this.description);
        videoDto.setVideoUrl(this.videoUrl);
        videoDto.setThumbnailUrl(this.thumbnailUrl);
        videoDto.setDateUploaded(this.dateUploaded);
        videoDto.setOwner(this.owner.toNoPasswordUserDto());
        videoDto.setCategory(this.category);
        videoDto.setNumberLikes(this.numberLikes);
        videoDto.setNumberDislikes(this.numberDislikes);
        videoDto.setComments(comments);
        return videoDto;
    }
}
