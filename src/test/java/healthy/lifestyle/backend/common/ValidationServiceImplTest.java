package healthy.lifestyle.backend.common;

import static org.junit.jupiter.api.Assertions.*;

import healthy.lifestyle.backend.base.UnitBaseTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

/**
 * @see ValidationServiceImpl
 */
class ValidationServiceImplTest extends UnitBaseTest {
    @InjectMocks
    ValidationServiceImpl validationService;

    @Test
    void checkStringPositive() {
        String input = "hello";

        boolean actual = validationService.checkText(input);

        assertTrue(actual);
    }

    @Test
    void checkStringNegative() {
        String input = "hello+";

        boolean actual = validationService.checkText(input);

        assertFalse(actual);
    }
}
