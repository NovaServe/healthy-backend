package healthy.lifestyle.backend.user.validation.annotation;

import healthy.lifestyle.backend.user.validation.UserValidationMessage;
import healthy.lifestyle.backend.user.validation.UserValidationUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsernameOptionalValidator implements ConstraintValidator<UsernameOptionalValidation, String> {
    @Autowired
    UserValidationUtil userValidationUtil;

    @Override
    public void initialize(UsernameOptionalValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true; // null values are considered valid
        int min = 5;
        int max = 20;
        if (value.isBlank() || value.length() < min || value.length() > max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(UserValidationMessage.USERNAME_LENGTH_RANGE.getName())
                    .addConstraintViolation();
            return false;
        }
        return userValidationUtil.isValidUsername(value);
    }
}
