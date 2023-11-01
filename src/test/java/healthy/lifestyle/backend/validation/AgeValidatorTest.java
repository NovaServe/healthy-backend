package healthy.lifestyle.backend.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgeValidatorTest {
    @InjectMocks
    AgeValidator ageValidator;

    @Test
    public void validationTest_shouldReturnTrue() {
        // Given
        int age = 20;

        // When
        boolean validated = ageValidator.validation(age);

        // Then
        assertTrue(validated);
    }

    @Test
    public void validationTest_shouldReturnFalse() {
        // Given
        int age = 2;

        // When
        boolean validated = ageValidator.validation(age);

        // Then
        assertFalse(validated);
    }

    @Test
    public void validationTest_shouldReturnTrue_whenNullProvided() {
        // Given
        Integer age = null;

        // When
        boolean validated = ageValidator.validation(age);

        // Then
        assertTrue(validated);
    }
}
