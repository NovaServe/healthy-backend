package healthy.lifestyle.backend.admin.user.service;

import healthy.lifestyle.backend.admin.user.repository.UserAdminRepository;
import healthy.lifestyle.backend.user.dto.UserResponseDto;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.repository.CountryRepository;
import healthy.lifestyle.backend.user.repository.RoleRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class UserAdminServiceImpl implements UserAdminService {

    private final UserAdminRepository userAdminRepository;

    private final RoleRepository roleRepository;

    private final CountryRepository countryRepository;

    private final ModelMapper modelMapper;

    public UserAdminServiceImpl(
            UserAdminRepository userAdminRepository,
            RoleRepository roleRepository,
            CountryRepository countryRepository,
            ModelMapper modelMapper) {
        this.userAdminRepository = userAdminRepository;
        this.roleRepository = roleRepository;
        this.countryRepository = countryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<UserResponseDto> getUsersWithFilter(
            Long roleId, String username, String email, String fullName, Long countryId, Integer age) {
        Optional<Role> role = roleId != null ? roleRepository.findById(roleId) : Optional.empty();
        Optional<Country> country = countryId != null ? countryRepository.findById(countryId) : Optional.empty();
        List<User> users = userAdminRepository.findWithFilter(
                role.orElse(null), username, email, fullName, country.orElse(null), age);

        return users.stream()
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .sorted(Comparator.comparing(UserResponseDto::getId))
                .toList();
    }
}
