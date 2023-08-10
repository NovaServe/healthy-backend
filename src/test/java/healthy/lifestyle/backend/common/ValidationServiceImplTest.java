package healthy.lifestyle.backend.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @see ValidationServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class ValidationServiceImplTest {
    @InjectMocks
    ValidationServiceImpl validationService;

    @Test
    void validatedText_Positive() {
        String input = "test";
        boolean actual = validationService.validatedText(input);
        assertTrue(actual);
    }

    @Test
    void validatedText_Negative() {
        String input = "test+";
        boolean actual = validationService.validatedText(input);
        assertFalse(actual);
    }
}
