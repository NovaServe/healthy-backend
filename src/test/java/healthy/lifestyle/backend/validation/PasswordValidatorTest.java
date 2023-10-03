package healthy.lifestyle.backend.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordValidatorTest {
    @InjectMocks
    PasswordValidator passwordValidator;

    @Test
    void validationTest_shouldReturnTrue() {
        // Given
        String input = "test-password";

        // When
        boolean actual = passwordValidator.validation(input);

        // Then
        assertTrue(actual);
    }

    @Test
    void validationTest_shouldReturnFalse() {
        // Given
        String input = "test/password";

        // When
        boolean actual = passwordValidator.validation(input);

        // Then
        assertFalse(actual);
    }

    @Test
    void validationTest_shouldReturnFalse_whenWhitespaceProvided() {
        // Given
        String input = "test password";

        // When
        boolean actual = passwordValidator.validation(input);

        // Then
        assertFalse(actual);
    }
}
