package healthy.lifestyle.backend.notification.firebase;

import com.google.firebase.messaging.*;
import healthy.lifestyle.backend.calendar.shared.service.DateTimeService;
import healthy.lifestyle.backend.notification.model.FirebaseUserToken;
import healthy.lifestyle.backend.shared.exception.ApiExceptionCustomMessage;
import healthy.lifestyle.backend.user.api.UserApi;
import healthy.lifestyle.backend.user.model.User;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {
    @Autowired
    FirebaseMessaging firebaseMessaging;

    @Autowired
    FirebaseUserTokenRepository firebaseUserTokenRepository;

    @Autowired
    UserApi userApi;

    @Autowired
    DateTimeService dateTimeService;

    private static final Logger logger = LoggerFactory.getLogger(FirebaseService.class);

    public void handleFirebaseUserToken(
            long userId, FirebaseController.FirebaseRequestDto requestDto, String userAgent) {
        if (requestDto.getFirebaseUserTokenFromLocalStorage() == null) {
            Optional<FirebaseUserToken> firebaseUserToken = firebaseUserTokenRepository.findByUser_IdAndToken(
                    userId, requestDto.getFirebaseUserTokenNowReceived());
            if (firebaseUserToken.isEmpty()) {
                User user = userApi.getUserById(userId);
                FirebaseUserToken newFirebaseUserToken = FirebaseUserToken.builder()
                        .token(requestDto.getFirebaseUserTokenNowReceived())
                        .userAgent(userAgent)
                        .user(user)
                        .createdAt(dateTimeService.getCurrentDate())
                        .build();
                firebaseUserTokenRepository.save(newFirebaseUserToken);
            }
        } else {
            if (!requestDto
                    .getFirebaseUserTokenFromLocalStorage()
                    .equals(requestDto.getFirebaseUserTokenNowReceived())) {
                Optional<FirebaseUserToken> firebaseUserTokenFromLocalStorage =
                        firebaseUserTokenRepository.findByUser_IdAndToken(
                                userId, requestDto.getFirebaseUserTokenFromLocalStorage());
                firebaseUserTokenFromLocalStorage.ifPresent(userToken -> firebaseUserTokenRepository.delete(userToken));

                Optional<FirebaseUserToken> firebaseUserTokenNowReceived =
                        firebaseUserTokenRepository.findByUser_IdAndToken(
                                userId, requestDto.getFirebaseUserTokenNowReceived());
                if (firebaseUserTokenNowReceived.isEmpty()) {
                    User user = userApi.getUserById(userId);
                    FirebaseUserToken newFirebaseUserToken = FirebaseUserToken.builder()
                            .token(requestDto.getFirebaseUserTokenNowReceived())
                            .userAgent(userAgent)
                            .user(user)
                            .createdAt(dateTimeService.getCurrentDate())
                            .build();
                    firebaseUserTokenRepository.save(newFirebaseUserToken);
                }
            }
        }
    }

    public void sendMessage(long userId, FirebaseMessageDto firebaseMessageDto) {
        List<FirebaseUserToken> firebaseUserTokens = firebaseUserTokenRepository.findByUser_Id(userId);
        if (!firebaseUserTokens.isEmpty()) {
            Notification notification = Notification.builder()
                    .setTitle(firebaseMessageDto.getTitle())
                    .setBody(firebaseMessageDto.getBody())
                    .setImage(firebaseMessageDto.getImage())
                    .build();

            for (FirebaseUserToken firebaseUserToken : firebaseUserTokens) {
                Message message = Message.builder()
                        .setToken(firebaseUserToken.getToken())
                        .setNotification(notification)
                        // .putAllData(firebaseMessageDto.getData())
                        .build();

                try {
                    firebaseMessaging.send(message);
                    logger.info(
                            "Firebase notification has been sent: userId={}, messageTitle={}, messageBody={}",
                            userId,
                            firebaseMessageDto.getTitle(),
                            firebaseMessageDto.getBody());
                } catch (FirebaseMessagingException e) {
                    if (e.getMessagingErrorCode().equals(MessagingErrorCode.UNREGISTERED)) {
                        long removedTokenId = firebaseUserToken.getId();
                        firebaseUserTokenRepository.delete(firebaseUserToken);
                        logger.info(
                                "Firebase user token has been removed because of MessagingErrorCode.UNREGISTERED, userId={}, removedTokenId={}",
                                userId,
                                removedTokenId);
                    } else {
                        throw new ApiExceptionCustomMessage(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
                    }
                }
            }
        }
    }
}
