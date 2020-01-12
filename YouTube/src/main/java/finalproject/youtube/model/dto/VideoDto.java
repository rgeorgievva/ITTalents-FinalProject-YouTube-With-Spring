package finalproject.youtube.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VideoDto {

    private long id;
    private String title;
    private LocalDateTime dateUploaded;
    private NoPasswordUserDto owner;
}
