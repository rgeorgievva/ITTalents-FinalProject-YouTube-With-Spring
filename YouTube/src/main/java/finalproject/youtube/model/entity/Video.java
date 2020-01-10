package finalproject.youtube.model.entity;

import finalproject.youtube.model.dto.VideoDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
    @Column(name = "owner_id")
    private long ownerId;
    @Column(name = "category_id")
    private long categoryId;
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
        videoDto.setDescription(this.description);
        videoDto.setVideoUrl(this.videoUrl);
        videoDto.setThumbnailUrl(this.thumbnailUrl);
        videoDto.setDateUploaded(this.dateUploaded);
        videoDto.setOwnerId(this.ownerId);
        videoDto.setCategoryId(this.categoryId);
        videoDto.setStatus(this.status);

        return videoDto;
    }

}
