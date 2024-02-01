package healthy.lifestyle.backend.admin.user.service;

import healthy.lifestyle.backend.user.dto.UserResponseDto;
import java.util.List;

public interface UserAdminService {
    List<UserResponseDto> getUsersWithFilter(
            Long roleId, String username, String email, String fullName, Long countryId, Integer age);
}
