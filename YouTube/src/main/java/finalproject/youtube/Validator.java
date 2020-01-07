package finalproject.youtube;

import finalproject.youtube.exceptions.BadRequestException;
import finalproject.youtube.model.dto.RegisterUserDto;
import finalproject.youtube.model.entity.Category;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALID_PASSWORD_STRENGTH =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    private static final Pattern VALID_USERNAME_FORMAT =
            Pattern.compile("^[a-zA-Z0-9._-]{3,}$");
    private static final Pattern VALID_VIDEO_TITLE_FORMAT =
            Pattern.compile("^[a-zA-Z0-9._ -]{5,}$");

    private static boolean validateEmail(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(email);
        return matcher.find();
    }

    private static boolean validatePasswordStrength(String password) {
        Matcher matcher = VALID_PASSWORD_STRENGTH.matcher(password);
        return matcher.find();
    }

    private static boolean validateConfirmPassword(String password, String confirmPassword) {
        if(password.equals(confirmPassword)) {
            return true;
        }
        return false;
    }

    private static boolean validateUsername(String username) {
        Matcher matcher = VALID_USERNAME_FORMAT.matcher(username);
        return matcher.find();
    }

    private static boolean validateVideoTitle(String title) {
        Matcher matcher = VALID_VIDEO_TITLE_FORMAT.matcher(title);
        return matcher.find();
    }


    public static void validateRegisterDto(RegisterUserDto registerUserDto) throws BadRequestException {
        String username = registerUserDto.getUsername();
        String email = registerUserDto.getEmail();
        String password = registerUserDto.getPassword();
        String confirmPassword = registerUserDto.getConfirmPassword();

        if (!validateUsername(username)) {
            throw new BadRequestException("Username should be at least 3 chars and should contain only " +
                    "latin letters, digits, points, dashes and underscores");
        }

        if (!validateEmail(email)) {
            throw new BadRequestException("Invalid email!");
        }

        if (!validatePasswordStrength(password)) {
            throw new BadRequestException("Password should contain at least 8 chars including at least 1 digit, " +
                    "1 upper case, 1 lower case letter, 1 special char (@, #, %, $, ^)  and should NOT contain " +
                    "spaces or tabs!");
        }

        if (!validateConfirmPassword(password, confirmPassword)) {
            throw new BadRequestException("Confirm password should match password!");
        }
    }

    public static void validateVideoInformation(String title, long id) throws BadRequestException {
        if (!validateVideoTitle(title)) {
            throw new BadRequestException("Video title should be at least 5 chars and should contain only " +
                    "latin letters, digits, points, dashes, underscores and spaces");
        }

        if (!Category.isValidId(id)) {
            throw new BadRequestException("Invalid category id!");
        }
    }
}
