package healthy.lifestyle.backend.shared.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidationUtilTest {
    @InjectMocks
    ValidationUtil validationUtil;

    @ParameterizedTest
    @MethodSource("healthy.lifestyle.backend.shared.validation.TitleArgs#getValidTitles")
    void isValidTitle_shouldReturnTrue_whenValidTitle(String title) {
        assertTrue(validationUtil.isValidTitle(title));
    }

    @ParameterizedTest
    @MethodSource("healthy.lifestyle.backend.shared.validation.TitleArgs#getInvalidTitles")
    void isValidTitle_shouldReturnFalse_whenInvalidTitle(String title) {
        assertFalse(validationUtil.isValidTitle(title));
    }

    @ParameterizedTest
    @MethodSource("healthy.lifestyle.backend.shared.validation.DescriptionArgs#getValidDescriptions")
    void isValidDescription_shouldReturnTrue_whenValidDescription(String description) {
        assertTrue(validationUtil.isValidDescription(description));
    }

    @ParameterizedTest
    @MethodSource("healthy.lifestyle.backend.shared.validation.DescriptionArgs#getInvalidDescriptions")
    void isValidDescription_shouldReturnFalse_whenInvalidDescription(String description) {
        assertFalse(validationUtil.isValidDescription(description));
    }

    @ParameterizedTest
    @MethodSource("healthy.lifestyle.backend.shared.validation.WebLinkArgs#getValidWebLinks")
    void isValidWebLink_shouldReturnTrue_whenValidWebLinks(String webLink) {
        assertTrue(validationUtil.isValidWebLink(webLink));
    }

    @ParameterizedTest
    @MethodSource("healthy.lifestyle.backend.shared.validation.WebLinkArgs#getInvalidWebLinks")
    void isValidWebLink_shouldReturnFalse_whenInvalidWebLinks(String webLink) {
        assertFalse(validationUtil.isValidWebLink(webLink));
    }

    @ParameterizedTest
    @MethodSource("healthy.lifestyle.backend.shared.validation.IdArgs#getValidIds")
    void isValidId_shouldReturnTrue_whenValidId(Long id) {
        assertTrue(validationUtil.isValidId(id));
    }

    @ParameterizedTest
    @MethodSource("healthy.lifestyle.backend.shared.validation.IdArgs#getInvalidIds")
    void isValidId_shouldReturnFalse_whenInvalidId(Long id) {
        assertFalse(validationUtil.isValidId(id));
    }

    @Test
    void isNotEmptyList_shouldReturnTrue_whenNotEmptyList() {
        assertTrue(validationUtil.isNotEmptyList(List.of(1L)));
    }

    @Test
    void isNotEmptyList_shouldReturnFalse_whenEmptyList() {
        assertFalse(validationUtil.isNotEmptyList(Collections.emptyList()));
    }
}
