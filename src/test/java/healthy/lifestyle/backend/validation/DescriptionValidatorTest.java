package healthy.lifestyle.backend.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DescriptionValidatorTest {
    @InjectMocks
    DescriptionValidator descriptionValidator;

    @Test
    void validationTest_shouldReturnTrue() {
        // Given
        String input = "Desc 1";

        // When
        boolean validated = descriptionValidator.validation(input);

        // Then
        assertTrue(validated);
    }

    @Test
    void validationTest_shouldReturnFalse() {
        // Given
        String input = "Desc 1 $";

        // When
        boolean validated = descriptionValidator.validation(input);

        // Then
        assertFalse(validated);
    }
}
