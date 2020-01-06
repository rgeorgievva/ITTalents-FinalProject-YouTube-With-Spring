package finalproject.youtube.model.dto;

import javax.validation.constraints.NotNull;

public class LoginUserDto {

    @NotNull
    private String email;
    @NotNull
    private String password;
}
