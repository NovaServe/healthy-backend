package healthy.lifestyle.backend.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsernameValidatorTest {
    @InjectMocks
    UsernameValidator usernameValidator;

    @Test
    void validation_Positive() {
        String input = "test-username";
        boolean actual = usernameValidator.validation(input);
        assertTrue(actual);
    }

    @Test
    void validation_Negative() {
        String input = "test-usernam e";
        boolean actual = usernameValidator.validation(input);
        assertFalse(actual);
    }
}
