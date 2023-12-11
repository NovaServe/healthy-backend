package healthy.lifestyle.backend.users.service;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.data.country.CountryTestBuilder;
import healthy.lifestyle.backend.data.user.UserDtoTestBuilder;
import healthy.lifestyle.backend.data.user.UserTestBuilder;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ApiExceptionCustomMessage;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.dto.UserResponseDto;
import healthy.lifestyle.backend.users.dto.UserUpdateRequestDto;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.repository.CountryRepository;
import healthy.lifestyle.backend.users.repository.UserRepository;
import java.util.Optional;
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
public class UserUpdateServiceTest {
    @InjectMocks
    UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CountryRepository countryRepository;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Spy
    ModelMapper modelMapper;

    UserTestBuilder userTestBuilder = new UserTestBuilder();

    UserDtoTestBuilder userDtoTestBuilder = new UserDtoTestBuilder();

    CountryTestBuilder countryTestBuilder = new CountryTestBuilder();

    @ParameterizedTest
    @MethodSource("updateUserMultipleValidInputs")
    void updateUserTest_shouldReturnUserResponseDto_whenValidInputGiven(
            String username,
            String email,
            String fullName,
            Long countryId,
            Integer age,
            String password,
            String confirmPassword)
            throws NoSuchFieldException, IllegalAccessException {
        // Given
        UserTestBuilder.UserWrapper userWrapper = userTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setCountryIdOrSeed(1)
                .buildUser();

        CountryTestBuilder.CountryTestWrapper countryWrapper = null;
        long initialCountryId = userWrapper.getCountryId();
        if (countryId != userWrapper.getCountryId()) {
            countryWrapper = countryTestBuilder.getWrapper();
            countryWrapper.setIdOrSeed(countryId).build();
        }

        UserDtoTestBuilder.UserDtoWrapper<UserUpdateRequestDto> userRequestDtoWrapper =
                userDtoTestBuilder.getWrapper(UserUpdateRequestDto.class);
        userRequestDtoWrapper.buildUserUpdateRequestDto();

        userRequestDtoWrapper.setFieldValue("countryId", countryId);
        if (nonNull(username)) userRequestDtoWrapper.setFieldValue("username", username);
        if (nonNull(email)) userRequestDtoWrapper.setFieldValue("email", email);
        if (nonNull(fullName)) userRequestDtoWrapper.setFieldValue("fullName", fullName);
        if (nonNull(age)) userRequestDtoWrapper.setFieldValue("age", age);
        if (nonNull(password) && nonNull(confirmPassword)) {
            userRequestDtoWrapper.setFieldValue("password", password);
            userRequestDtoWrapper.setFieldValue("confirmPassword", confirmPassword);
        }

        // Mocking
        when(userRepository.findById(userWrapper.getUserId())).thenReturn(Optional.ofNullable(userWrapper.getUser()));
        if (countryId != userWrapper.getCountryId())
            when(countryRepository.findById((Long) userRequestDtoWrapper.getFieldValue("countryId")))
                    .thenReturn(Optional.ofNullable(countryWrapper.get()));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        // When
        UserResponseDto responseDto = userService.updateUser(userWrapper.getUserId(), userRequestDtoWrapper.getDto());

        // Then
        verify(userRepository, times(1)).findById(userWrapper.getUserId());
        if (countryId != initialCountryId)
            verify(countryRepository, times(1)).findById((Long) userRequestDtoWrapper.getFieldValue("countryId"));
        else verify(countryRepository, times(0)).findById((Long) userRequestDtoWrapper.getFieldValue("countryId"));
        verify(userRepository, times(1)).save(any(User.class));

        if (nonNull(username)) assertEquals(userRequestDtoWrapper.getFieldValue("username"), responseDto.getUsername());
        else assertEquals(userWrapper.getFieldValue("username"), responseDto.getUsername());

        if (nonNull(email)) assertEquals(userRequestDtoWrapper.getFieldValue("email"), responseDto.getEmail());
        else assertEquals(userWrapper.getFieldValue("email"), responseDto.getEmail());

        if (nonNull(fullName)) assertEquals(userRequestDtoWrapper.getFieldValue("fullName"), responseDto.getFullName());
        else assertEquals(userWrapper.getFieldValue("fullName"), responseDto.getFullName());

        if (countryId != initialCountryId)
            assertEquals(userRequestDtoWrapper.getFieldValue("countryId"), responseDto.getCountryId());
        else assertEquals(userWrapper.getCountryId(), responseDto.getCountryId());

        if (nonNull(age)) assertEquals(userRequestDtoWrapper.getFieldValue("age"), responseDto.getAge());
        else assertEquals(userWrapper.getFieldValue("age"), responseDto.getAge());

        if (nonNull(password) && nonNull(confirmPassword))
            assertTrue(passwordEncoder.matches((String) userRequestDtoWrapper.getFieldValue("password"), (String)
                    userWrapper.getFieldValue("password")));
    }

    static Stream<Arguments> updateUserMultipleValidInputs() {
        return Stream.of(
                Arguments.of("new-username", null, null, 1L, null, null, null),
                Arguments.of(null, "new-email@email.com", null, 1L, null, null, null),
                Arguments.of(null, null, "New full name", 1L, null, null, null),
                Arguments.of(null, null, null, 2L, null, null, null),
                Arguments.of(null, null, null, 1L, 110, null, null),
                Arguments.of(null, null, null, 1L, null, "new-password", "new-password"));
    }

    @ParameterizedTest
    @MethodSource("updateUserMultipleValidInputsAreNotDifferent")
    void updateUserTest_shouldThrowErrorAnd400_whenValidButNotDifferentInputGiven(
            String username, String email, String fullName, Integer age, String password, String confirmPassword)
            throws NoSuchFieldException, IllegalAccessException {
        // Given
        UserTestBuilder.UserWrapper userWrapper = userTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setCountryIdOrSeed(1)
                .buildUser();
        if (nonNull(age)) userWrapper.setFieldValue("age", age);

        UserDtoTestBuilder.UserDtoWrapper<UserUpdateRequestDto> userRequestDtoWrapper =
                userDtoTestBuilder.getWrapper(UserUpdateRequestDto.class);
        userRequestDtoWrapper.buildUserUpdateRequestDto();
        if (nonNull(username)) userRequestDtoWrapper.setFieldValue("username", username);
        if (nonNull(email)) userRequestDtoWrapper.setFieldValue("email", email);
        if (nonNull(fullName)) userRequestDtoWrapper.setFieldValue("fullName", fullName);
        if (nonNull(age)) userRequestDtoWrapper.setFieldValue("age", age);
        if (nonNull(password) && nonNull(confirmPassword)) {
            userRequestDtoWrapper.setFieldValue("password", password);
            userRequestDtoWrapper.setFieldValue("confirmPassword", confirmPassword);
        }

        // Mocking
        when(userRepository.findById(userWrapper.getUserId())).thenReturn(Optional.ofNullable(userWrapper.getUser()));

        // When
        ApiExceptionCustomMessage exception = assertThrows(
                ApiExceptionCustomMessage.class,
                () -> userService.updateUser(userWrapper.getUserId(), userRequestDtoWrapper.getDto()));

        // Then
        verify(userRepository, times(1)).findById(userWrapper.getUserId());
        verify(countryRepository, times(0)).findById((Long) userRequestDtoWrapper.getFieldValue("countryId"));
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

    static Stream<Arguments> updateUserMultipleValidInputsAreNotDifferent() {
        return Stream.of(
                Arguments.of("Username-1", null, null, null, null, null),
                Arguments.of(null, "email-1@email.com", null, null, null, null),
                Arguments.of(null, null, "Full name 1", null, null, null),
                Arguments.of(null, null, null, 20, null, null),
                Arguments.of(null, null, null, null, "Password-1", "Password-1"),
                Arguments.of("Username-1", "email-1@email.com", "Full name 1", 20, "Password-1", "Password-1"));
    }

    @Test
    void updateUserTest_shouldThrowErrorAnd404_whenUserNotFound() throws NoSuchFieldException, IllegalAccessException {
        // Given
        UserTestBuilder.UserWrapper userWrapper = userTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setCountryIdOrSeed(1)
                .buildUser();

        UserDtoTestBuilder.UserDtoWrapper<UserUpdateRequestDto> userRequestDtoWrapper =
                userDtoTestBuilder.getWrapper(UserUpdateRequestDto.class);
        userRequestDtoWrapper.buildUserUpdateRequestDto();

        // Mocking
        when(userRepository.findById(userWrapper.getUserId())).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(
                ApiException.class,
                () -> userService.updateUser(userWrapper.getUserId(), userRequestDtoWrapper.getDto()));

        // Then
        verify(userRepository, times(1)).findById(userWrapper.getUserId());
        verify(countryRepository, times(0)).findById((Long) userRequestDtoWrapper.getFieldValue("countryId"));
        verify(userRepository, times(0)).save(any(User.class));

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpStatus().value());
        assertEquals(ErrorMessage.USER_NOT_FOUND.getName(), exception.getMessage());
    }

    @Test
    void updateUserTest_shouldThrowErrorAnd500_whenCountryNotFound()
            throws NoSuchFieldException, IllegalAccessException {
        // Given
        UserTestBuilder.UserWrapper userWrapper = userTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setCountryIdOrSeed(1)
                .buildUser();

        UserDtoTestBuilder.UserDtoWrapper<UserUpdateRequestDto> userRequestDtoWrapper =
                userDtoTestBuilder.getWrapper(UserUpdateRequestDto.class);
        userRequestDtoWrapper.buildUserUpdateRequestDto();

        long wrongCountryId = userWrapper.getCountryId() + 1;
        userRequestDtoWrapper.setFieldValue("countryId", wrongCountryId);

        // Mocking
        when(userRepository.findById(userWrapper.getUserId())).thenReturn(Optional.ofNullable(userWrapper.getUser()));
        when(countryRepository.findById(wrongCountryId)).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(
                ApiException.class,
                () -> userService.updateUser(userWrapper.getUserId(), userRequestDtoWrapper.getDto()));

        // Then
        verify(userRepository, times(1)).findById(userWrapper.getUserId());
        verify(countryRepository, times(1)).findById(wrongCountryId);
        verify(userRepository, times(0)).save(any(User.class));

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                exception.getHttpStatus().value());
        assertEquals(ErrorMessage.COUNTRY_NOT_FOUND.getName(), exception.getMessage());
    }
}
