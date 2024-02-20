package healthy.lifestyle.backend.user.validation.annotation;

import healthy.lifestyle.backend.user.validation.UserValidationMessage;
import healthy.lifestyle.backend.user.validation.UserValidationUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator implements ConstraintValidator<PasswordValidation, String> {
    @Autowired
    UserValidationUtil userValidationUtil;

    @Override
    public void initialize(PasswordValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        int min = 10;
        int max = 20;
        if (value == null || value.isBlank() || value.length() < min || value.length() > max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(UserValidationMessage.PASSWORD_LENGTH_RANGE.getName())
                    .addConstraintViolation();
            return false;
        }
        return userValidationUtil.isValidPassword(value);
    }
}
