package healthy.lifestyle.backend.user.service;

import org.springframework.security.core.Authentication;

public interface AuthUtil {
    Long getUserIdFromAuthentication(Authentication authentication);

    void checkAuthUserIdAndParamUserId(Authentication authentication, long userId);
}
