package healthy.lifestyle.backend.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpValidatorTest {
    @InjectMocks
    HttpValidator httpValidator;

    @Test
    void validationTest_shouldReturnTrue() {
        // Given
        String input = "https://ref.com";

        // When
        boolean actual = httpValidator.validation(input);

        // Then
        assertTrue(actual);
    }

    @Test
    void validationTest_shouldReturnFalse() {
        // Given
        String input = "test";

        // When
        boolean actual = httpValidator.validation(input);

        // Then
        assertFalse(actual);
    }
}
