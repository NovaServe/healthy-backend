package healthy.lifestyle.backend.users.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import healthy.lifestyle.backend.data.DataUtil;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.dto.SignupRequestDto;
import healthy.lifestyle.backend.users.dto.SignupResponseDto;
import healthy.lifestyle.backend.users.dto.UserResponseDto;
import healthy.lifestyle.backend.users.dto.UserUpdateRequestDto;
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
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
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

    @Spy
    ModelMapper modelMapper;

    @Test
    void createUserTest_shouldReturnUserDto() {
        // Given
        Integer age = 20;
        SignupRequestDto signupRequestDto = dataUtil.createSignupRequestDto("one", 1L, age);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        Role role = Role.builder().id(1L).name("ROLE_USER").build();
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
        assertEquals(age, signupRequestDto.getAge());
    }

    @Test
    void updateUserTest_shouldReturnUserDto() {
        // Given
        Country country = Country.builder().id(1L).name("Country").build();
        User user = dataUtil.createUserEntity(1);
        user.setCountry(country);

        UserUpdateRequestDto updateUserRequestDto = dataUtil.createUpdateUserRequestDto("one", 1L, 25);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(countryRepository.findById(updateUserRequestDto.getCountryId()))
                .thenReturn(Optional.of(country));
        when(userRepository.save(user)).thenReturn(user);

        // When
        UserResponseDto updatedUserResponse = userService.updateUser(user.getId(), updateUserRequestDto);

        // Then
        assertEquals(updatedUserResponse.getUsername(), updateUserRequestDto.getUsername());
        assertEquals(updatedUserResponse.getEmail(), updateUserRequestDto.getEmail());
        assertEquals(updatedUserResponse.getFullName(), updateUserRequestDto.getFullName());
        assertEquals(25, updatedUserResponse.getAge());
        assertEquals(1L, updatedUserResponse.getCountryId());
        assertEquals(user.getId(), updatedUserResponse.getId());
    }

    @Test
    void updateUserTest_shouldReturnServerErrorAnd500_whenUserNotFound() {
        // Given
        User user = dataUtil.createUserEntity(2L);
        UpdateUserRequestDto updateUserRequestDto = dataUtil.createUpdateUserRequestDto("one", 1L, 25);
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // When
        ApiException exception =
                assertThrows(ApiException.class, () -> userService.updateUser(user.getId(), updateUserRequestDto));

        // Then
        verify(userRepository, times(1)).findById(user.getId());
        verify(modelMapper, never()).map(any(), eq(UserResponseDto.class));
        assertEquals(ErrorMessage.SERVER_ERROR.getName(), exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    @Test
    void deleteUserTest_shouldReturnDeletedUserId() {
        // Given
        User user = dataUtil.createUserEntity(1);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When
        long deletedId = userService.deleteUser(user.getId());

        // Then
        verify(userRepository, times(1)).findById(user.getId());

        assertEquals(user.getId(), deletedId);
    }

    @Test
    void deleteUserTest_shouldReturnNotFoundAnd400_whenUserNotFound() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(ApiException.class, () -> userService.deleteUser(1L));

        // Then
        verify(userRepository, times(1)).findById(anyLong());

        assertEquals(ErrorMessage.USER_NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    void getUserDetailsByIdTest_shouldReturnUserDto() {
        // Given
        User user = dataUtil.createUserEntity(1L);
        UserResponseDto expectedDto =
                new UserResponseDto(1L, "username-1", "Full Name 1", "username-1@email.com", 1L, 30);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserResponseDto.class)).thenReturn(expectedDto);

        // When
        UserResponseDto actualDto = userService.getUserDetailsById(user.getId());

        // Then
        verify(userRepository, times(1)).findById(user.getId());
        verify(modelMapper, times(1)).map(user, UserResponseDto.class);
        assertEquals(expectedDto, actualDto);
    }

    @Test
    void getUserDetailsByIdTest_shouldReturnNotFoundAnd400_whenUserNotFound() {
        // Given
        User user = dataUtil.createUserEntity(2L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(ApiException.class, () -> userService.getUserDetailsById(user.getId()));

        // Then
        verify(userRepository, times(1)).findById(user.getId());
        verify(modelMapper, never()).map(any(), eq(UserResponseDto.class));
        assertEquals(ErrorMessage.USER_NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }
}
