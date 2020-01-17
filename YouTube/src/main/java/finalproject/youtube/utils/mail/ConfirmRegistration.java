package finalproject.youtube.utils.mail;

import finalproject.youtube.model.pojo.User;
import finalproject.youtube.model.repository.UserRepository;

import java.util.Random;

public class ConfirmRegistration extends Thread {

    private static final String elements = "abcdefghjiklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final int FIRST_NUM_FORMULA = 587;
    public static final int SECOND_NUM_FORMULA = 987;
    public static final int ENCRYPTED_URL_LENGTH = 40;
    private static final String SUBJECT = "Welcome to YoutubeTalents!";
    private String BODY    = "To finalize your registration, " +
            "please click on the link below to verify your account:\n"
            +"localhost:8080/users/verify/";
    private             User   user;
    private UserRepository userRepository;

    public ConfirmRegistration(User user, UserRepository userRepository){
        this.user = user;
        this.userRepository = userRepository;
    }

    @Override
    public void run() {
        String verificationURL = encryptURL();
        this.user.setVerificationURL(verificationURL);
        this.user.setStatus(User.UserStatus.NEW.toString());
        long secretNumber = (user.getId() * FIRST_NUM_FORMULA) - SECOND_NUM_FORMULA;
        BODY = BODY.concat(Long.toString(secretNumber)).concat("/").concat(verificationURL);
        this.userRepository.save(user);
        MailSender.sendMail(user.getEmail(), SUBJECT, BODY);
    }

    public static long decryptId(long encryptedId){
        return (encryptedId + SECOND_NUM_FORMULA) / FIRST_NUM_FORMULA;
    }

    private static String encryptURL(){
        StringBuilder url = new StringBuilder();
        for (int i = 0; i < ENCRYPTED_URL_LENGTH; i++) {
            url.append(elements.charAt(new Random().nextInt(elements.length())));
        }
        return  url.toString();
    }
}
