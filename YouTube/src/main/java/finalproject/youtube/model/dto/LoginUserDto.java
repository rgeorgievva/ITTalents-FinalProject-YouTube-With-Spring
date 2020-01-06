package finalproject.youtube.model.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class LoginUserDto {

    @NotNull
    private String email;
    @NotNull
    private String password;
}
