package healthy.lifestyle.backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import healthy.lifestyle.backend.activity.workout.model.BodyPart;
import healthy.lifestyle.backend.activity.workout.model.Exercise;
import healthy.lifestyle.backend.activity.workout.model.HttpRef;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.activity.workout.service.RemovalServiceImpl;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ApiExceptionCustomMessage;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.shared.util.VerificationUtil;
import healthy.lifestyle.backend.testutil.DtoUtil;
import healthy.lifestyle.backend.testutil.TestUtil;
import healthy.lifestyle.backend.user.dto.SignupRequestDto;
import healthy.lifestyle.backend.user.dto.UserResponseDto;
import healthy.lifestyle.backend.user.dto.UserUpdateRequestDto;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.Timezone;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.repository.CountryRepository;
import healthy.lifestyle.backend.user.repository.RoleRepository;
import healthy.lifestyle.backend.user.repository.TimezoneRepository;
import healthy.lifestyle.backend.user.repository.UserRepository;
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
    private TimezoneRepository timezoneRepository;

    @Mock
    private RemovalServiceImpl removalService;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Spy
    private VerificationUtil verificationUtil;

    @Spy
    private TestUtil testUtil;

    @Spy
    private DtoUtil dtoUtil;

    @Spy
    ModelMapper modelMapper;

    @Test
    void createUser_shouldReturnDto_whenValidRequest() {
        // Given
        Role role = testUtil.createUserRole();
        Country country = testUtil.createCountry(1);
        Timezone timezone = testUtil.createTimezone(1);
        SignupRequestDto requestDto = dtoUtil.signupRequestDto(1, 1L, timezone.getId());

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(roleRepository.findByName(role.getName())).thenReturn(Optional.of(role));
        when(countryRepository.findById(country.getId())).thenReturn(Optional.of(country));
        when(timezoneRepository.findById(timezone.getId())).thenReturn(Optional.of(timezone));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            User saved = (User) args[0];
            saved.setId(1L);
            return saved;
        });

        // When
        userService.createUser(requestDto);

        // Then
        verify(userRepository, times(1)).existsByEmail(requestDto.getEmail());
        verify(userRepository, times(1)).existsByUsername(requestDto.getUsername());
        verify(roleRepository, times(1)).findByName(role.getName());
        verify(countryRepository, times(1)).findById(country.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getUserDetailsById_shouldReturnDto_whenValidId() {
        // Given
        User user = testUtil.createUser(1);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When
        UserResponseDto responseDto = userService.getUserDetailsById(user.getId());

        // Then
        verify(userRepository, times(1)).findById(user.getId());
        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("countryId", "timezoneId")
                .isEqualTo(user);
        assertEquals(user.getCountry().getId(), responseDto.getCountryId());
    }

    @Test
    void getUserDetailsById_shouldThrowErrorWith404_whenUserNotFound() {
        // Given
        User user = testUtil.createUser(2);
        ApiException expectedException =
                new ApiException(ErrorMessage.USER_NOT_FOUND, user.getId(), HttpStatus.NOT_FOUND);
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // When
        ApiException actualException =
                assertThrows(ApiException.class, () -> userService.getUserDetailsById(user.getId()));

        // Then
        verify(userRepository, times(1)).findById(user.getId());
        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatus(), actualException.getHttpStatus());
    }

    @ParameterizedTest
    @MethodSource("updateUserValidFields")
    void updateUser_shouldReturnUpdatedDto_whenValidFields(
            String username,
            String email,
            String fullName,
            Long countryId,
            Integer age,
            String password,
            String confirmPassword)
            throws NoSuchFieldException, IllegalAccessException {
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

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        if (countryId == 2L) when(countryRepository.findById(countryId)).thenReturn(Optional.of(newCountry));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        // When
        UserResponseDto responseDto = userService.updateUser(user.getId(), requestDto);

        // Then
        verify(userRepository, times(1)).findById(user.getId());

        if (countryId == 2L) verify(countryRepository, times(1)).findById(countryId);
        else verify(countryRepository, times(0)).findById(countryId);

        verify(userRepository, times(1)).save(any(User.class));

        if (username != null) assertEquals(requestDto.getUsername(), responseDto.getUsername());
        else assertEquals(initialUsername, responseDto.getUsername());

        if (email != null) assertEquals(requestDto.getEmail(), responseDto.getEmail());
        else assertEquals(initialEmail, responseDto.getEmail());

        if (fullName != null) assertEquals(requestDto.getFullName(), responseDto.getFullName());
        else assertEquals(initialFullName, responseDto.getFullName());

        if (countryId == 2L) assertEquals(requestDto.getCountryId(), responseDto.getCountryId());
        else assertEquals(initialCountryId, responseDto.getCountryId());

        if (age != null) assertEquals(requestDto.getAge(), responseDto.getAge());
        else assertEquals(initialAge, responseDto.getAge());

        if (password != null && confirmPassword != null)
            assertTrue(passwordEncoder.matches(requestDto.getPassword(), user.getPassword()));
        else assertTrue(passwordEncoder.matches(initialPassword, user.getPassword()));
    }

    static Stream<Arguments> updateUserValidFields() {
        return Stream.of(
                Arguments.of("new-username", null, null, 1L, null, null, null),
                Arguments.of(null, "new-email@email.com", null, 1L, null, null, null),
                Arguments.of(null, null, "New full name", 1L, null, null, null),
                Arguments.of(null, null, null, 2L, null, null, null),
                Arguments.of(null, null, null, 1L, 100, null, null),
                Arguments.of(null, null, null, 1L, null, "new-password", "new-password"));
    }

    @Test
    void updateUser_shouldThrowErrorWith400_whenNotDifferentFields() {
        // Given
        User user = testUtil.createUser(1);
        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        requestDto.setUsername(user.getUsername());
        requestDto.setEmail(user.getEmail());
        requestDto.setFullName(user.getFullName());
        requestDto.setCountryId(user.getCountry().getId());
        requestDto.setPassword("Password-1");
        requestDto.setConfirmPassword("Password-1");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When
        ApiExceptionCustomMessage exception =
                assertThrows(ApiExceptionCustomMessage.class, () -> userService.updateUser(user.getId(), requestDto));

        // Then
        verify(userRepository, times(1)).findById(user.getId());
        verify(countryRepository, times(0)).findById(requestDto.getCountryId());
        verify(userRepository, times(0)).save(any(User.class));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus().value());
        assertEquals(
                ErrorMessage.FIELDS_VALUES_ARE_NOT_DIFFERENT.getName() + "username, email, fullName, password",
                exception.getMessage());
    }

    @Test
    void updateUser_shouldThrowErrorWith404_whenUserNotFound() {
        // Given
        User user = testUtil.createUser(1);
        long nonExistentUserId = 1000L;
        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        requestDto.setCountryId(user.getId());
        requestDto.setUsername("New-username");
        ApiException expectedException =
                new ApiException(ErrorMessage.USER_NOT_FOUND, nonExistentUserId, HttpStatus.NOT_FOUND);
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When
        ApiException actualException =
                assertThrows(ApiException.class, () -> userService.updateUser(nonExistentUserId, requestDto));

        // Then
        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(countryRepository, times(0)).findById(anyLong());
        verify(userRepository, times(0)).save(any(User.class));

        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatus(), actualException.getHttpStatus());
    }

    @Test
    void updateUser_shouldThrowErrorWith500_whenCountryNotFound() {
        // Given
        User user = testUtil.createUser(1);
        long nonExistentCountryId = 1000L;
        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        requestDto.setUsername("New-username");
        requestDto.setCountryId(nonExistentCountryId);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(countryRepository.findById(nonExistentCountryId)).thenReturn(Optional.empty());
        ApiException expectedException =
                new ApiException(ErrorMessage.COUNTRY_NOT_FOUND, nonExistentCountryId, HttpStatus.NOT_FOUND);

        // When
        ApiException actualException =
                assertThrows(ApiException.class, () -> userService.updateUser(user.getId(), requestDto));

        // Then
        verify(userRepository, times(1)).findById(user.getId());
        verify(countryRepository, times(1)).findById(nonExistentCountryId);
        verify(userRepository, times(0)).save(any(User.class));

        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void deleteUser_shouldReturnVoid_whenValidId() {
        // Given
        User user = testUtil.createUser(1);

        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        BodyPart bodyPart2 = testUtil.createBodyPart(2);
        HttpRef customHttRef1 = testUtil.createCustomHttpRef(1, user);
        HttpRef customHttRef2 = testUtil.createCustomHttpRef(2, user);
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(2);
        boolean needsEquipment = true;

        Exercise customExercise1 = testUtil.createCustomExercise(
                1, needsEquipment, List.of(bodyPart1, bodyPart2), List.of(customHttRef1, customHttRef2), user);
        Exercise customExercise2 = testUtil.createCustomExercise(
                2, needsEquipment, List.of(bodyPart1, bodyPart2), List.of(customHttRef1, defaultHttpRef), user);
        Exercise defaultExercise = testUtil.createDefaultExercise(
                1, needsEquipment, List.of(bodyPart1, bodyPart2), List.of(defaultHttpRef));

        Workout customWorkout1 = testUtil.createCustomWorkout(1, List.of(customExercise1, customExercise2), user);
        Workout customWorkout2 = testUtil.createCustomWorkout(2, List.of(customExercise1, defaultExercise), user);
        Workout defaultWorkout = testUtil.createDefaultWorkout(3, List.of(defaultExercise));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        doNothing().when(removalService).deleteCustomWorkouts(anyList());
        doNothing().when(removalService).deleteCustomExercises(anyList());
        doNothing().when(removalService).deleteCustomHttpRefs(anyList());
        doNothing().when(userRepository).delete(any(User.class));

        // When
        userService.deleteUser(user.getId());

        // Then
        verify(userRepository, times(1)).findById(user.getId());
        verify(removalService, times(1)).deleteCustomWorkouts(user.getWorkoutsIdsSorted());
        verify(removalService, times(1)).deleteCustomExercises(user.getExercisesIdsSorted());
        verify(removalService, times(1)).deleteCustomHttpRefs(user.getHttpRefsIdsSorted());
        verify(userRepository, times(1)).delete(any(User.class));
    }

    @Test
    void deleteUser_shouldThrowErrorWith404_whenUserNotFound() {
        // Given
        long randomUserId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.USER_NOT_FOUND, randomUserId, HttpStatus.NOT_FOUND);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        ApiException actualException = assertThrows(ApiException.class, () -> userService.deleteUser(randomUserId));

        // Then
        verify(userRepository, times(1)).findById(anyLong());

        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatus(), actualException.getHttpStatus());
    }
}
