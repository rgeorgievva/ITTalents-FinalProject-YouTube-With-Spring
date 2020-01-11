package finalproject.youtube.model.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Getter
@Setter
public class NoPasswordUserDto {

    private long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime dateCreated;
}
