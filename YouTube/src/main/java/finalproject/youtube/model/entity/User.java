package finalproject.youtube.model.entity;

import finalproject.youtube.model.dto.NoPasswordUserDto;
import finalproject.youtube.model.dto.RegisterUserDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column
    private String username;
    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column
    private String email;
    @Column
    private String password;
    @Column
    private LocalDateTime dateCreated;

    public User(String username, String firstName, String lastName, String email, String password) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
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

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", dateCreated=" + dateCreated +
                '}';
    }
}
