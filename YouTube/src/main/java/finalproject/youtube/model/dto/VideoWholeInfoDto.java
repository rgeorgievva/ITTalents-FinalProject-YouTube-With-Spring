package finalproject.youtube.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import finalproject.youtube.model.pojo.Category;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class VideoWholeInfoDto {

    private long id;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime dateUploaded;
    private NoPasswordUserDto owner;
    private Category category;
    private int numberLikes;
    private int numberDislikes;
    private List<ResponseCommentWithRepliesDto> comments;
}
