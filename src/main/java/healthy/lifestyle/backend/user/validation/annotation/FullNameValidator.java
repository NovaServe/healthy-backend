package healthy.lifestyle.backend.user.validation.annotation;

import healthy.lifestyle.backend.user.validation.UserValidationMessage;
import healthy.lifestyle.backend.user.validation.UserValidationUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FullNameValidator implements ConstraintValidator<FullNameValidation, String> {
    @Autowired
    UserValidationUtil userValidationUtil;

    @Override
    public void initialize(FullNameValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        int min = 2;
        int max = 50;
        if (value == null || value.isBlank() || value.length() < min || value.length() > max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(UserValidationMessage.FULL_NAME_LENGTH_RANGE.getName())
                    .addConstraintViolation();
            return false;
        }
        return userValidationUtil.isValidFullName(value);
    }
}
