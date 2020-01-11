package finalproject.youtube.model.entity;

import finalproject.youtube.model.dto.NoPasswordUserDto;
import finalproject.youtube.model.dto.RegisterUserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    private enum UserStatus{
        VERIFIED, NEW;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "user_name")
    private String username;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "email")
    private String email;
    @Column(name = "password")
    private String password;
    @Column(name = "date_created")
    private LocalDateTime dateCreated;
    @Column
    private UserStatus userStatus;
    @Column
    private int verificationCode;

    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static User registerDtoToUser(RegisterUserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPassword(userDto.getPassword());
        user.setEmail(userDto.getEmail());

        return user;
    }

    public NoPasswordUserDto toNoPasswordUserDto() {
        NoPasswordUserDto noPasswordUserDto = new NoPasswordUserDto();
        noPasswordUserDto.setId(this.id);
        noPasswordUserDto.setUsername(this.username);
        noPasswordUserDto.setFirstName(this.firstName);
        noPasswordUserDto.setLastName(this.lastName);
        noPasswordUserDto.setEmail(this.email);
        noPasswordUserDto.setDateCreated(this.dateCreated);

        return noPasswordUserDto;
    }
}
