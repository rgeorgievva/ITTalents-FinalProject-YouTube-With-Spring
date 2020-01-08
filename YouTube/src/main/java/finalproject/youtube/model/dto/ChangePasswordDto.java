package finalproject.youtube.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ChangePasswordDto {

    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
