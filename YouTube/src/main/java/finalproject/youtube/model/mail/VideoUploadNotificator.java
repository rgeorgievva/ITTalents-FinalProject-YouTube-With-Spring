package finalproject.youtube.model.mail;

import finalproject.youtube.MailSender;
import finalproject.youtube.model.dao.UserDAO;
import finalproject.youtube.model.entity.User;
import lombok.SneakyThrows;

import java.util.List;

public class VideoUploadNotificator extends Thread {

    private UserDAO userDAO;
    private User subscribedTo;

    private final String subject = "Video Upload Notification";
    private final String body;

    public VideoUploadNotificator(UserDAO userDAO, User subscribedTo) {
        this.userDAO = userDAO;
        this.subscribedTo = subscribedTo;
        this.body = "User " + subscribedTo.getUsername() + " uploaded new video! Go check it out!";
    }


    @SneakyThrows
    @Override
    public void run() {
        List<String> subscribersEmails = userDAO.getSubscribers(subscribedTo.getId());
        for (String email : subscribersEmails) {
            MailSender.sendMail(email, subject, body);
        }
    }
}
