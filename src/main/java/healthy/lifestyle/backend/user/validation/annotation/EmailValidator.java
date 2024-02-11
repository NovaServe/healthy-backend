package healthy.lifestyle.backend.user.validation.annotation;

import healthy.lifestyle.backend.user.validation.UserValidationMessage;
import healthy.lifestyle.backend.user.validation.UserValidationUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailValidator implements ConstraintValidator<EmailValidation, String> {
    @Autowired
    UserValidationUtil userValidationUtil;

    @Override
    public void initialize(EmailValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        int min = 7;
        int max = 60;
        if (value == null || value.isBlank() || value.length() < min || value.length() > max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(UserValidationMessage.EMAIL_LENGTH_RANGE.getName())
                    .addConstraintViolation();
            return false;
        }
        return userValidationUtil.isValidEmail(value);
    }
}
