package healthy.lifestyle.backend.users.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import healthy.lifestyle.backend.data.DataUtil;
import healthy.lifestyle.backend.users.dto.SignupRequestDto;
import healthy.lifestyle.backend.users.dto.SignupResponseDto;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.repository.CountryRepository;
import healthy.lifestyle.backend.users.repository.RoleRepository;
import healthy.lifestyle.backend.users.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CountryRepository countryRepository;

    @Spy
    private PasswordEncoder passwordEncoder;

    @Spy
    private DataUtil dataUtil;

    @Test
    void createUserTest_shouldReturnUserDto() {
        // Given
        SignupRequestDto signupRequestDto = dataUtil.createSignupRequestDto("one", 1L);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        Role role = new Role.Builder().id(1L).name("ROLE_USER").build();
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.ofNullable(role));
        Country country = Country.builder().id(1L).name("Country").build();
        when(countryRepository.getReferenceById(1L)).thenReturn(country);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            User saved = (User) args[0];
            saved.setId(1L);
            return saved;
        });

        // When
        SignupResponseDto responseDto = userService.createUser(signupRequestDto);

        // Then
        assertEquals(1L, responseDto.getId());
    }
}
