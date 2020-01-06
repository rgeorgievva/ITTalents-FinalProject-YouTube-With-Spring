package finalproject.youtube.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VideoDto {

    private int id;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private long duration;
    private LocalDateTime dateUploaded;
    private int ownerId;
    private int categoryId;
}
