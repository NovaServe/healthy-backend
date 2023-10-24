package healthy.lifestyle.backend.common;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.AuthService;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthUtil {
    private final AuthService authService;

    public AuthUtil(AuthService authService) {
        this.authService = authService;
    }

    public Long getUserIdFromAuthentication(Authentication authentication) {
        if (isNull(authentication) || !authentication.isAuthenticated()) return null;
        String usernameOrEmail = authentication.getName();
        Optional<User> userOptional = authService.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
        return userOptional.map(User::getId).orElse(null);
    }
}
