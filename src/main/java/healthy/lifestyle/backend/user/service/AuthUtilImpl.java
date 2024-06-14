package healthy.lifestyle.backend.user.service;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthUtilImpl implements AuthUtil {
    @Autowired
    UserRepository userRepository;

    @Override
    public Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        String usernameOrEmail = authentication.getName();
        return userRepository
                .findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .map(User::getId)
                .orElse(null);
    }

    @Override
    public void checkAuthUserIdAndParamUserId(Authentication authentication, long userId) {
        Long authUserId = this.getUserIdFromAuthentication(authentication);
        if (authUserId == null || authUserId != userId)
            throw new ApiException(ErrorMessage.USER_REQUESTED_ANOTHER_USER_PROFILE, null, HttpStatus.BAD_REQUEST);
    }
}
