package healthy.lifestyle.backend.admin.users.service;

import healthy.lifestyle.backend.users.dto.UserResponseDto;
import healthy.lifestyle.backend.users.model.Country;
import java.util.List;

public interface AdminService {
    List<UserResponseDto> getUsersByFilters(
            String username, String email, String fullName, Country country, Integer age);
}
