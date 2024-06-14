package healthy.lifestyle.backend.user.api;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.repository.UserRepository;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UserApiImpl implements UserApi {
    @Autowired
    UserRepository userRepository;

    @Override
    public User getUserById(long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new ApiException(ErrorMessage.USER_NOT_FOUND, userId, HttpStatus.BAD_REQUEST));
    }

    @Override
    public TimeZone getUserTimeZone(long userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ApiException(ErrorMessage.USER_NOT_FOUND, userId, HttpStatus.BAD_REQUEST));
        return TimeZone.getTimeZone(user.getTimezone().getName());
    }
}
