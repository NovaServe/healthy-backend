package healthy.lifestyle.backend.shared.validation.annotation;

import healthy.lifestyle.backend.shared.validation.ValidationMessage;
import healthy.lifestyle.backend.shared.validation.ValidationUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebLinkValidator implements ConstraintValidator<WebLinkValidation, String> {
    @Autowired
    ValidationUtil validationUtil;

    @Override
    public void initialize(WebLinkValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ValidationMessage.NOT_NULL_OR_BLANK.getName())
                    .addConstraintViolation();
            return false;
        }
        return validationUtil.isValidWebLink(value);
    }
}
