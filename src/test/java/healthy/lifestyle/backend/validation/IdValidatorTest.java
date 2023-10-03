package healthy.lifestyle.backend.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IdValidatorTest {
    @InjectMocks
    IdValidator idValidator;

    @Test
    void validationTest_shouldReturnTrue() {
        // Given
        long id = 0;

        // When
        boolean validated = idValidator.validation(id);

        // Then
        assertTrue(validated);
    }

    @Test
    void validationTest_shouldReturnFalse() {
        // Given
        long id = -1;

        // When
        boolean validated = idValidator.validation(id);

        // Then
        assertFalse(validated);
    }

    @Test
    void validationTest_shouldReturnFalse_whenNullProvided() {
        // Given
        Long id = null;

        // When
        boolean validated = idValidator.validation(id);

        // Then
        assertFalse(validated);
    }
}
