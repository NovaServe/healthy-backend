package healthy.lifestyle.backend.notification.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import healthy.lifestyle.backend.shared.exception.ApiExceptionCustomMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class FirebaseMessagingService {
    @Autowired
    FirebaseMessaging firebaseMessaging;

    private static final Logger logger = LoggerFactory.getLogger(FirebaseMessagingService.class);

    public void sendMessage(FirebaseMessageDto firebaseMessageDto) {

        Notification notification = Notification.builder()
                .setTitle(firebaseMessageDto.getTitle())
                .setBody(firebaseMessageDto.getBody())
                .setImage(firebaseMessageDto.getImage())
                .build();

        Message message = Message.builder()
                .setToken(firebaseMessageDto.getFirebaseUserToken())
                .setNotification(notification)
                // .putAllData(firebaseMessageDto.getData())
                .build();

        try {
            firebaseMessaging.send(message);
            logger.info("Message has been sent {}, {}", firebaseMessageDto.getTitle(), firebaseMessageDto.getBody());
        } catch (FirebaseMessagingException e) {
            throw new ApiExceptionCustomMessage(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
