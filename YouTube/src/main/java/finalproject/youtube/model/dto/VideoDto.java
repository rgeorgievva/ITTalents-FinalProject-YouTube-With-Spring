package finalproject.youtube.model.dto;

import finalproject.youtube.model.pojo.Category;
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
    private NoPasswordUserDto owner;
    private Category category;
    private int numberLikes;
    private int numberDislikes;
}
