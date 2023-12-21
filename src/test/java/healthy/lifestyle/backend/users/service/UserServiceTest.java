package healthy.lifestyle.backend.users.service;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ApiExceptionCustomMessage;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.dto.SignupRequestDto;
import healthy.lifestyle.backend.users.dto.UserResponseDto;
import healthy.lifestyle.backend.users.dto.UserUpdateRequestDto;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.repository.CountryRepository;
import healthy.lifestyle.backend.users.repository.RoleRepository;
import healthy.lifestyle.backend.users.repository.UserRepository;
import healthy.lifestyle.backend.util.DtoUtil;
import healthy.lifestyle.backend.util.TestUtil;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.model.Workout;
import healthy.lifestyle.backend.workout.service.RemovalServiceImpl;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    @Mock
    private RemovalServiceImpl removalService;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Spy
    private TestUtil testUtil;

    @Spy
    private DtoUtil dtoUtil;

    @Spy
    ModelMapper modelMapper;

    @Test
    void createUserTest_shouldReturnUserDto() {
        // Given
        Integer age = 20;
        SignupRequestDto signupRequestDto = dtoUtil.signupRequestDto(1, 1L, age);
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
        userService.createUser(signupRequestDto);

        // Then
    }

    @Test
    void getUserDetailsByIdTest_shouldReturnUserDto() {
        // Given
        User user = testUtil.createUser(1);
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
    void getUserDetailsByIdTest_shouldThrowErrorWith400_whenUserNotFound() {
        // Given
        User user = testUtil.createUser(2);
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(ApiException.class, () -> userService.getUserDetailsById(user.getId()));

        // Then
        verify(userRepository, times(1)).findById(user.getId());
        verify(modelMapper, never()).map(any(), eq(UserResponseDto.class));
        assertEquals(ErrorMessage.USER_NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @ParameterizedTest
    @MethodSource("updateUserMultipleValidInputs")
    void updateUserTest_shouldReturnUserResponseDto_whenValidInput(
            String username,
            String email,
            String fullName,
            Long countryId,
            Integer age,
            String password,
            String confirmPassword) {
        // Given
        User user = testUtil.createUser(1);
        Country newCountry = testUtil.createCountry(2);
        String initialUsername = user.getUsername();
        String initialEmail = user.getEmail();
        String initialFullName = user.getFullName();
        long initialCountryId = user.getCountry().getId();
        int initialAge = user.getAge();
        String initialPassword = "Password-1";

        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        if (countryId == 2L) requestDto.setCountryId(newCountry.getId());
        else requestDto.setCountryId(user.getCountry().getId());
        requestDto.setUsername(username);
        requestDto.setEmail(email);
        requestDto.setFullName(fullName);
        requestDto.setAge(age);
        requestDto.setPassword(password);
        requestDto.setConfirmPassword(confirmPassword);

        // Mocking
        when(userRepository.findById(user.getId())).thenReturn(Optional.ofNullable(user));
        if (countryId == 2L) when(countryRepository.findById(countryId)).thenReturn(Optional.ofNullable(newCountry));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        // When
        UserResponseDto responseDto = userService.updateUser(user.getId(), requestDto);

        // Then
        verify(userRepository, times(1)).findById(user.getId());

        if (countryId == 2L) verify(countryRepository, times(1)).findById(countryId);
        else verify(countryRepository, times(0)).findById(countryId);

        verify(userRepository, times(1)).save(any(User.class));

        if (nonNull(username)) assertEquals(requestDto.getUsername(), responseDto.getUsername());
        else assertEquals(initialUsername, responseDto.getUsername());

        if (nonNull(email)) assertEquals(requestDto.getEmail(), responseDto.getEmail());
        else assertEquals(initialEmail, responseDto.getEmail());

        if (nonNull(fullName)) assertEquals(requestDto.getFullName(), responseDto.getFullName());
        else assertEquals(initialFullName, responseDto.getFullName());

        if (countryId == 2L) assertEquals(requestDto.getCountryId(), responseDto.getCountryId());
        else assertEquals(initialCountryId, responseDto.getCountryId());

        if (nonNull(age)) assertEquals(requestDto.getAge(), responseDto.getAge());
        else assertEquals(initialAge, responseDto.getAge());

        if (nonNull(password) && nonNull(confirmPassword))
            assertTrue(passwordEncoder.matches(requestDto.getPassword(), user.getPassword()));
        else assertTrue(passwordEncoder.matches(initialPassword, user.getPassword()));
    }

    static Stream<Arguments> updateUserMultipleValidInputs() {
        return Stream.of(
                Arguments.of("new-username", null, null, 1L, null, null, null),
                Arguments.of(null, "new-email@email.com", null, 1L, null, null, null),
                Arguments.of(null, null, "New full name", 1L, null, null, null),
                Arguments.of(null, null, null, 2L, null, null, null),
                Arguments.of(null, null, null, 1L, 100, null, null),
                Arguments.of(null, null, null, 1L, null, "new-password", "new-password"));
    }

    @ParameterizedTest
    @MethodSource("updateUserMultipleValidInputsButNotDifferent")
    void updateUserTest_shouldThrowErrorWith400_whenValidButNotDifferentInput(
            String username, String email, String fullName, Integer age, String password, String confirmPassword) {
        // Given
        User user = testUtil.createUser(1);

        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        if (nonNull(age)) {
            user.setAge(age);
            requestDto.setAge(age);
        }
        requestDto.setUsername(username);
        requestDto.setEmail(email);
        requestDto.setFullName(fullName);
        requestDto.setCountryId(user.getCountry().getId());
        requestDto.setPassword(password);
        requestDto.setConfirmPassword(confirmPassword);

        // Mocking
        when(userRepository.findById(user.getId())).thenReturn(Optional.ofNullable(user));

        // When
        ApiExceptionCustomMessage exception =
                assertThrows(ApiExceptionCustomMessage.class, () -> userService.updateUser(user.getId(), requestDto));

        // Then
        verify(userRepository, times(1)).findById(user.getId());
        verify(countryRepository, times(0)).findById(requestDto.getCountryId());
        verify(userRepository, times(0)).save(any(User.class));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus().value());

        boolean allFieldsAreNotDifferent = nonNull(username)
                && nonNull(email)
                && nonNull(fullName)
                && nonNull(age)
                && nonNull(password)
                && nonNull(confirmPassword);
        if (allFieldsAreNotDifferent) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append(ErrorMessage.USERNAME_IS_NOT_DIFFERENT.getName());
            errorMessage.append(" ");
            errorMessage.append(ErrorMessage.EMAIL_IS_NOT_DIFFERENT.getName());
            errorMessage.append(" ");
            errorMessage.append(ErrorMessage.FULLNAME_IS_NOT_DIFFERENT.getName());
            errorMessage.append(" ");
            errorMessage.append(ErrorMessage.AGE_IS_NOT_DIFFERENT.getName());
            errorMessage.append(" ");
            errorMessage.append(ErrorMessage.PASSWORD_IS_NOT_DIFFERENT.getName());
            assertEquals(errorMessage.toString(), exception.getMessage());
        } else {
            if (nonNull(username))
                assertEquals(ErrorMessage.USERNAME_IS_NOT_DIFFERENT.getName(), exception.getMessage());
            if (nonNull(email)) assertEquals(ErrorMessage.EMAIL_IS_NOT_DIFFERENT.getName(), exception.getMessage());
            if (nonNull(fullName))
                assertEquals(ErrorMessage.FULLNAME_IS_NOT_DIFFERENT.getName(), exception.getMessage());
            if (nonNull(age)) assertEquals(ErrorMessage.AGE_IS_NOT_DIFFERENT.getName(), exception.getMessage());
            if (nonNull(password) && nonNull(confirmPassword))
                assertEquals(ErrorMessage.PASSWORD_IS_NOT_DIFFERENT.getName(), exception.getMessage());
        }
    }

    static Stream<Arguments> updateUserMultipleValidInputsButNotDifferent() {
        return Stream.of(
                Arguments.of("Username-1", null, null, null, null, null),
                Arguments.of(null, "email-1@email.com", null, null, null, null),
                Arguments.of(null, null, "Full Name One", null, null, null),
                Arguments.of(null, null, null, 20, null, null),
                Arguments.of(null, null, null, null, "Password-1", "Password-1"),
                Arguments.of("Username-1", "email-1@email.com", "Full Name One", 20, "Password-1", "Password-1"));
    }

    @Test
    void updateUserTest_shouldThrowErrorWith404_whenUserNotFound() {
        // Given
        User user = testUtil.createUser(1);
        long wrongUserId = user.getId() + 1;

        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        requestDto.setCountryId(user.getId());
        requestDto.setUsername("New-username");

        // Mocking
        when(userRepository.findById(wrongUserId)).thenReturn(Optional.empty());

        // When
        ApiException exception =
                assertThrows(ApiException.class, () -> userService.updateUser(wrongUserId, requestDto));

        // Then
        verify(userRepository, times(1)).findById(wrongUserId);
        verify(countryRepository, times(0)).findById(anyLong());
        verify(userRepository, times(0)).save(any(User.class));

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpStatus().value());
        assertEquals(ErrorMessage.USER_NOT_FOUND.getName(), exception.getMessage());
    }

    @Test
    void updateUserTest_shouldThrowErrorWith500_whenCountryNotFound() {
        // Given
        Role role = testUtil.createUserRole(1);
        Country country = testUtil.createCountry(1);
        User user = testUtil.createUser(1, role, country);
        long wrongCountryId = user.getCountry().getId() + 1;

        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        requestDto.setUsername("New-username");
        requestDto.setCountryId(wrongCountryId);

        // Mocking
        when(userRepository.findById(user.getId())).thenReturn(Optional.ofNullable(user));
        when(countryRepository.findById(wrongCountryId)).thenReturn(Optional.empty());

        // When
        ApiException exception =
                assertThrows(ApiException.class, () -> userService.updateUser(user.getId(), requestDto));

        // Then
        verify(userRepository, times(1)).findById(user.getId());
        verify(countryRepository, times(1)).findById(wrongCountryId);
        verify(userRepository, times(0)).save(any(User.class));

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                exception.getHttpStatus().value());
        assertEquals(ErrorMessage.COUNTRY_NOT_FOUND.getName(), exception.getMessage());
    }

    @Test
    void deleteUserTest_shouldReturnVoid() {
        // Given
        User user = User.builder().id(1L).build();

        BodyPart bodyPart1 = BodyPart.builder().id(1L).name("Body part 1").build();
        HttpRef customHttpRef =
                HttpRef.builder().id(1L).name("Ref 1").isCustom(true).user(user).build();
        Exercise customExercise = Exercise.builder()
                .id(1L)
                .title("Exercise 1")
                .description("Desc 1")
                .isCustom(true)
                .user(user)
                .bodyParts(Set.of(bodyPart1))
                .httpRefs(Set.of(customHttpRef))
                .build();

        BodyPart bodyPart2 = BodyPart.builder().id(2L).name("Body part 2").build();
        HttpRef defaultHttpRef =
                HttpRef.builder().id(2L).isCustom(false).name("Ref 2").build();
        Exercise defaultExercise = Exercise.builder()
                .id(2L)
                .title("Exercise 2")
                .description("Desc 2")
                .isCustom(false)
                .bodyParts(Set.of(bodyPart2))
                .httpRefs(Set.of(defaultHttpRef))
                .build();

        Workout customWorkout = Workout.builder()
                .id(1L)
                .title("Workout 1")
                .description("Workout Desc 1")
                .isCustom(true)
                .user(user)
                .exercises(Set.of(customExercise, defaultExercise))
                .build();

        user.setHttpRefs(new HashSet<>(List.of(customHttpRef)));
        user.setExercises(new HashSet<>(List.of(customExercise)));
        user.setWorkouts(new HashSet<>(List.of(customWorkout)));

        Workout defaultWorkout = Workout.builder()
                .id(2L)
                .title("Workout 2")
                .description("Workout Desc 2")
                .isCustom(false)
                .exercises(Set.of(defaultExercise))
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(any(User.class));
        doNothing().when(removalService).deleteCustomWorkouts(anyList());
        doNothing().when(removalService).deleteCustomExercises(anyList());
        doNothing().when(removalService).deleteCustomHttpRefs(anyList());

        // When
        userService.deleteUser(user.getId());

        // Then
        verify(userRepository, times(1)).findById(user.getId());
        verify(userRepository, times(1)).delete(any(User.class));
        verify(removalService, times(1)).deleteCustomWorkouts(anyList());
        verify(removalService, times(1)).deleteCustomExercises(anyList());
        verify(removalService, times(1)).deleteCustomHttpRefs(anyList());
    }

    @Test
    void deleteUserTest_shouldThrowErrorWith404_whenUserNotFound() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(ApiException.class, () -> userService.deleteUser(1L));

        // Then
        verify(userRepository, times(1)).findById(anyLong());

        assertEquals(ErrorMessage.USER_NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }
}
