package healthy.lifestyle.backend.users.service;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.repository.UserRepository;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByUsernameOrEmail(String username, String email) {
        return userRepository.findByUsernameOrEmail(username, email);
    }

    public Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        String usernameOrEmail = authentication.getName();
        return findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .map(User::getId)
                .orElse(null);
    }

    public void checkAuthUserIdAndParamUserId(Authentication authentication, long userId) {
        Long authUserId = this.getUserIdFromAuthentication(authentication);
        if (authUserId == null || authUserId != userId)
            throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, HttpStatus.BAD_REQUEST);
    }
}
