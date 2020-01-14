package finalproject.youtube.utils.mail;

import finalproject.youtube.model.pojo.User;
import finalproject.youtube.model.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;

//todo crypt user id with math formula

public class ConfirmRegistration extends Thread {

    private static final String SUBJECT = "Welcome to YoutubeTalents!";
    private String BODY    = "To finalize your registration, " +
            "please click on the link below to verify your account:\n"
            +"yt-ittalents.com/users/verify/";
    private             User   user;
    private UserRepository userRepository;

    public ConfirmRegistration(User user, UserRepository userRepository){
        this.user = user;
        this.userRepository = userRepository;
    }

    @Override
    public void run() {
        String verificationURL = RandomStringUtils.randomAlphanumeric(40);
        this.user.setVerificationURL(verificationURL);
        this.user.setStatus(User.UserStatus.NEW.toString());
        BODY = BODY.concat(Long.toString(user.getId())).concat("/").concat(verificationURL);
        this.userRepository.save(user);
        MailSender.sendMail(user.getEmail(), SUBJECT, BODY);
    }
}
