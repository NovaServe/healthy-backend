package healthy.lifestyle.backend.user.service;

import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.repository.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserUtilImpl implements UserUtil {
    private final UserRepository userRepository;

    public UserUtilImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> getUserByUsernameOrEmail(String username, String email) {
        return userRepository.findByUsernameOrEmail(username, email);
    }
}
