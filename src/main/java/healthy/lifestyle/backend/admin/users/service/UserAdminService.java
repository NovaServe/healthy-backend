package healthy.lifestyle.backend.admin.users.service;

import healthy.lifestyle.backend.users.dto.UserResponseDto;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import java.util.List;

public interface UserAdminService {
    List<UserResponseDto> getUsersByFilters(
            Role role, String username, String email, String fullName, Country country, Integer age);
}
