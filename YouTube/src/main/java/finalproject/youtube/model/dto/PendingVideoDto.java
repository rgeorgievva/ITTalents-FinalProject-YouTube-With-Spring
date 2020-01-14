package finalproject.youtube.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import finalproject.youtube.model.pojo.Category;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PendingVideoDto {

    private long id;
    private String title;
    private String description;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime dateUploaded;
    private NoPasswordUserDto owner;
    private Category category;
    private String status;
}
