package finalproject.youtube.model.mail;

import finalproject.youtube.MailSender;
import finalproject.youtube.model.entity.User;
import finalproject.youtube.model.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;

public class ConfirmRegistration extends Thread {

    private static final String SUBJECT = "Welcome to YoutubeTalents!";
    private String BODY    = "To finalize your registration, " +
            "please click on the link below to verify your account:\n"
            +"yt-ittalents.com/verify/";
    private             User   user;
    private UserRepository userRepository;
    private String verificationURL = RandomStringUtils.randomAlphanumeric(40);

    public ConfirmRegistration(User user, UserRepository userRepository){
        this.user = user;
        this.userRepository = userRepository;
        user.setVerificationURL(verificationURL);
        user.setStatus(User.UserStatus.NEW.toString());
        BODY = BODY.concat(this.verificationURL);
        userRepository.save(user);
    }

    @Override
    public void run() {
        MailSender.sendMail(user.getEmail(), SUBJECT, BODY);
    }
}
