package healthy.lifestyle.backend.users.service;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.repository.UserRepository;
import java.util.Optional;
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
        if (isNull(authentication) || !authentication.isAuthenticated()) return null;
        String usernameOrEmail = authentication.getName();
        Optional<User> userOptional = findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
        return userOptional.map(User::getId).orElse(null);
    }
}
