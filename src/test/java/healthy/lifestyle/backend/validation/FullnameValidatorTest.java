package healthy.lifestyle.backend.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FullnameValidatorTest {
    @InjectMocks
    FullNameValidator fullnameValidator;

    @Test
    void validationTest_shouldReturnTrue() {
        // Given
        String input = "Test Full Name";

        // When
        boolean actual = fullnameValidator.validation(input);

        // Then
        assertTrue(actual);
    }

    @Test
    void validationTest_shouldReturnFalse() {
        // Given
        String input = "Test Full Name +";

        // When
        boolean actual = fullnameValidator.validation(input);

        // Then
        assertFalse(actual);
    }
}
