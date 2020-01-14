package finalproject.youtube.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
@Setter
public class SmallUserDto {
    @NotNull
    private long id;
    @NotNull
    private String username;
}
