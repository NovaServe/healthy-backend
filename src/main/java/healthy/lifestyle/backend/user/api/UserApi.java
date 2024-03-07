package healthy.lifestyle.backend.user.api;

import healthy.lifestyle.backend.user.model.User;

public interface UserApi {
    User getUserById(long userId);
}
