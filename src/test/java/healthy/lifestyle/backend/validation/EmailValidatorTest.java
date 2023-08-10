package healthy.lifestyle.backend.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailValidatorTest {
    @InjectMocks
    EmailValidator emailValidator;

    @Test
    void validation_Positive() {
        String input = "test@email.com";
        boolean actual = emailValidator.validation(input);
        assertTrue(actual);
    }

    @Test
    void validation_Negative() {
        String input = "tes$t@email.com";
        boolean actual = emailValidator.validation(input);
        assertFalse(actual);
    }

    @Test
    void validation_Negative_Whitespace() {
        String input = "tes t@email.com";
        boolean actual = emailValidator.validation(input);
        assertFalse(actual);
    }
}
