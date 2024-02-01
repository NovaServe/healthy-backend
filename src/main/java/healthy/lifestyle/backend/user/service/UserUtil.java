package healthy.lifestyle.backend.user.service;

import healthy.lifestyle.backend.user.model.User;
import java.util.Optional;

public interface UserUtil {
    Optional<User> getUserByUsernameOrEmail(String username, String email);
}
