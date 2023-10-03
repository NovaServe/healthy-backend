package healthy.lifestyle.backend.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailValidatorTest {
    @InjectMocks
    EmailValidator emailValidator;

    @Test
    void validationTest_shouldReturnTrue() {
        // Given
        String input = "test@email.com";

        // When
        boolean actual = emailValidator.validation(input);

        // Then
        assertTrue(actual);
    }

    @Test
    void validationTest_shouldReturnFalse() {
        // Given
        String input = "tes$t@email.com";

        // When
        boolean actual = emailValidator.validation(input);

        // Then
        assertFalse(actual);
    }

    @Test
    void validationTest_shouldReturnFalse_whenWhitespace() {
        // Given
        String input = "tes t@email.com";

        // When
        boolean actual = emailValidator.validation(input);

        // Then
        assertFalse(actual);
    }
}
