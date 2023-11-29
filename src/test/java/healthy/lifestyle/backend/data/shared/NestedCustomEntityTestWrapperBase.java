package healthy.lifestyle.backend.data.shared;

import healthy.lifestyle.backend.users.model.User;

public interface NestedCustomEntityTestWrapperBase {
    NestedCustomEntityTestWrapperBase setIsCustom(boolean isCustom);

    NestedCustomEntityTestWrapperBase setUser(User user);

    User getUser();
}
