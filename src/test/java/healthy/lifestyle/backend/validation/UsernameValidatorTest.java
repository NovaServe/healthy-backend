package healthy.lifestyle.backend.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsernameValidatorTest {
    @InjectMocks
    UsernameValidator usernameValidator;

    @Test
    void validationTest_shouldReturnTrue() {
        // Given
        String input = "test-username";

        // When
        boolean actual = usernameValidator.validation(input);

        // Then
        assertTrue(actual);
    }

    @Test
    void validationTest_shouldReturnFalse() {
        // Given
        String input = "test-usernam e";

        // When
        boolean actual = usernameValidator.validation(input);

        // Then
        assertFalse(actual);
    }
}
