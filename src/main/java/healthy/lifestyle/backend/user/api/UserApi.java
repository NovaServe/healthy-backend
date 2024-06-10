package healthy.lifestyle.backend.user.api;

import healthy.lifestyle.backend.user.model.User;
import java.util.TimeZone;

public interface UserApi {
    User getUserById(long userId);

    TimeZone getUserTimeZone(long userId);
}
