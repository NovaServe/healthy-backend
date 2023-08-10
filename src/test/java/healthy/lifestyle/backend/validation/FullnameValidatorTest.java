package healthy.lifestyle.backend.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @see FullnameValidator
 * @see FullnameValidation
 * @see healthy.lifestyle.backend.users.dto.SignupRequestDto
 */
@ExtendWith(MockitoExtension.class)
class FullnameValidatorTest {
    @InjectMocks
    FullnameValidator fullnameValidator;

    @Test
    void validation_Positive() {
        String input = "Test Full Name";
        boolean actual = fullnameValidator.validation(input);
        assertTrue(actual);
    }

    @Test
    void validation_Negative() {
        String input = "Test Full Name +";
        boolean actual = fullnameValidator.validation(input);
        assertFalse(actual);
    }
}
