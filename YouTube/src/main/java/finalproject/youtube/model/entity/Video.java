package finalproject.youtube.model.entity;

import finalproject.youtube.model.dto.VideoDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column
    private String title;
    @Column
    private String description;
    @Column
    private String videoUrl;
    @Column
    private String thumbnailUrl;
    @Column
    private long duration;
    @Column
    private LocalDateTime dateUploaded;
    @Column
    private int ownerId;
    @Column
    private int categoryId;

    public Video(int id, String title, String description, String videoUrl, String thumbnailUrl, long duration,
                 LocalDateTime dateUploaded, int ownerId, int categoryId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.duration = duration;
        this.dateUploaded = dateUploaded;
        this.ownerId = ownerId;
        this.categoryId = categoryId;
    }

    public VideoDto toVideoDto() {
        VideoDto videoDto = new VideoDto();
        videoDto.setId(this.id);
        videoDto.setTitle(this.title);
        videoDto.setDescription(this.description);
        videoDto.setVideoUrl(this.videoUrl);
        videoDto.setThumbnailUrl(this.thumbnailUrl);
        videoDto.setDuration(this.duration);
        videoDto.setDateUploaded(this.dateUploaded);
        videoDto.setOwnerId(this.ownerId);
        videoDto.setCategoryId(this.categoryId);

        return videoDto;
    }

    @Override
    public String toString() {
        return "Video{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", duration=" + duration +
                ", dateUploaded=" + dateUploaded +
                ", ownerId=" + ownerId +
                ", categoryId=" + categoryId +
                '}';
    }
}
