package finalproject.youtube.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "videos")
public class Video {

    @Id
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
