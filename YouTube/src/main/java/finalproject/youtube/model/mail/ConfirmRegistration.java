package finalproject.youtube.model.mail;

import finalproject.youtube.model.entity.User;

import java.util.Random;

public class ConfirmRegistration extends Thread {

    private User user;
    private static final int MAX_VC = 9999;
    private static final int MIN_VC = 1000;
    private int verificationCode = new Random().nextInt(MAX_VC - MIN_VC) + MIN_VC;

    @Override
    public void run() {
        //add code to db
        //send code
    }
}
