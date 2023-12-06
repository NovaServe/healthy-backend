package healthy.lifestyle.backend.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotEmptyListValidatorTest {
    @InjectMocks
    NotEmptyListValidator validator;

    @Test
    public void validationTest_shouldReturnTrue_whenNotEmptyListGiven() {
        // Given
        List<Long> list = new ArrayList<>() {
            {
                add(1L);
                add(2L);
            }
        };

        // When
        boolean validated = validator.validation(list);

        // Then
        assertTrue(validated);
    }

    @Test
    public void validationTest_shouldReturnTrue_whenNullInputGiven() {
        // Given
        List<Long> list = null;

        // When
        boolean validated = validator.validation(list);

        // Then
        assertTrue(validated);
    }

    @Test
    public void validationTest_shouldReturnFalse_whenEmptyListGiven() {
        // Given
        List<Long> list = new ArrayList<>();

        // When
        boolean validated = validator.validation(list);

        // Then
        assertFalse(validated);
    }
}
