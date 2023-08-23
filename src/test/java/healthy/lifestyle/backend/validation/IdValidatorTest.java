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
    void validationPositive() {
        long id = 0;
        boolean validated = idValidator.validation(id);
        assertTrue(validated);
    }

    @Test
    void validationNegative() {
        long id = -1;
        boolean validated = idValidator.validation(id);
        assertFalse(validated);
    }

    @Test
    void validationNegativeNull() {
        boolean validated = idValidator.validation(null);
        assertFalse(validated);
    }
}
