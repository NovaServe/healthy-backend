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
    void validationPositive() {
        String input = "Desc 1";
        boolean validated = descriptionValidator.validation(input);
        assertTrue(validated);
    }

    @Test
    void validationNegative() {
        String input = "Desc 1 $";
        boolean validated = descriptionValidator.validation(input);
        assertFalse(validated);
    }
}
