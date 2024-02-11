package healthy.lifestyle.backend.user.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import healthy.lifestyle.backend.shared.validation.ValidationUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserValidationUtilTest {
    @InjectMocks
    UserValidationUtil userValidationUtil;

    @Spy
    ValidationUtil validationUtil;

    @ParameterizedTest
    @MethodSource("healthy.lifestyle.backend.user.validation.EmailArgs#getValidEmails")
    void isValidEmail_shouldReturnTrue_whenValidEmail(String email) {
        assertTrue(userValidationUtil.isValidEmail(email));
    }

    @ParameterizedTest
    @MethodSource("healthy.lifestyle.backend.user.validation.EmailArgs#getInvalidEmails")
    void isValidEmail_shouldReturnFalse_whenInvalidEmail(String email) {
        assertFalse(userValidationUtil.isValidEmail(email));
    }

    @ParameterizedTest
    @MethodSource("healthy.lifestyle.backend.user.validation.UsernameArgs#getValidUsernames")
    void isValidUsername_shouldReturnTrue_whenValidUsername(String username) {
        assertTrue(userValidationUtil.isValidUsername(username));
    }

    @ParameterizedTest
    @MethodSource("healthy.lifestyle.backend.user.validation.UsernameArgs#getInvalidUsernames")
    void isValidUsername_shouldReturnFalse_whenInvalidUsername(String username) {
        assertFalse(userValidationUtil.isValidUsername(username));
    }

    @ParameterizedTest
    @MethodSource("healthy.lifestyle.backend.user.validation.FullNameArgs#getValidFullNames")
    void isValidFullName_shouldReturnTrue_whenValidFullName(String fullName) {
        assertTrue(userValidationUtil.isValidFullName(fullName));
    }

    @ParameterizedTest
    @MethodSource("healthy.lifestyle.backend.user.validation.FullNameArgs#getInvalidFullNames")
    void isValidFullName_shouldReturnFalse_whenInvalidFullName(String fullName) {
        assertFalse(userValidationUtil.isValidFullName(fullName));
    }

    @ParameterizedTest
    @MethodSource("healthy.lifestyle.backend.user.validation.PasswordArgs#getValidPasswords")
    void isValidPassword_shouldReturnTrue_whenValidPassword(String password) {
        assertTrue(userValidationUtil.isValidPassword(password));
    }

    @ParameterizedTest
    @MethodSource("healthy.lifestyle.backend.user.validation.PasswordArgs#getInvalidPasswords")
    void isValidPassword_shouldReturnFalse_whenInvalidPassword(String password) {
        assertFalse(userValidationUtil.isValidPassword(password));
    }
}
