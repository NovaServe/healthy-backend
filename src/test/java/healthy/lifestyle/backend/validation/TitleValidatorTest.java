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
    void validationPositive() {
        String input = "Test title";
        boolean validated = titleValidator.validation(input);
        assertTrue(validated);
    }

    @Test
    void validationNegative() {
        String input = "Test title$";
        boolean validated = titleValidator.validation(input);
        assertFalse(validated);
    }
}
