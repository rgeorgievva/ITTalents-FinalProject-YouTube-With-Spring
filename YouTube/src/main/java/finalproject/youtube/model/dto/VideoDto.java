package finalproject.youtube.model.dto;

import finalproject.youtube.model.entity.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VideoDto {

    private long id;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private LocalDateTime dateUploaded;
    private long ownerId;
    private long categoryId;
    private String status;
    private int numberLikes;
    private int numberDislikes;
}
