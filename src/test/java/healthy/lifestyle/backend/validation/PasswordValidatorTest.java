package healthy.lifestyle.backend.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordValidatorTest {
    @InjectMocks
    PasswordValidator passwordValidator;

    @Test
    void validation_Positive() {
        String input = "test-password";
        boolean actual = passwordValidator.validation(input);
        assertTrue(actual);
    }

    @Test
    void validation_Negative() {
        String input = "test/password";
        boolean actual = passwordValidator.validation(input);
        assertFalse(actual);
    }

    @Test
    void validation_Negative_Whitespace() {
        String input = "test password";
        boolean actual = passwordValidator.validation(input);
        assertFalse(actual);
    }
}
