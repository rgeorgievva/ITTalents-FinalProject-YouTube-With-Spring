package finalproject.youtube.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
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
