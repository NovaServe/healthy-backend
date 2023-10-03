package healthy.lifestyle.backend.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TitleValidatorTest {
    @InjectMocks
    TitleValidator titleValidator;

    @Test
    void validationTest_shouldReturnTrue() {
        // Given
        String input = "Test title";

        // When
        boolean validated = titleValidator.validation(input);

        // Then
        assertTrue(validated);
    }

    @Test
    void validationTest_shouldReturnFalse() {
        // Given
        String input = "Test title$";

        // When
        boolean validated = titleValidator.validation(input);

        // Then
        assertFalse(validated);
    }
}
