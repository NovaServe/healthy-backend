package healthy.lifestyle.backend.user.validation.annotation;

import healthy.lifestyle.backend.user.validation.UserValidationMessage;
import healthy.lifestyle.backend.user.validation.UserValidationUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsernameOrEmailValidator implements ConstraintValidator<UsernameOrEmailValidation, String> {
    @Autowired
    UserValidationUtil userValidationUtil;

    private String usernameMessage;

    private String emailMessage;

    private String blank;

    @Override
    public void initialize(UsernameOrEmailValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.usernameMessage = constraintAnnotation.usernameMessage();
        this.emailMessage = constraintAnnotation.emailMessage();
        this.blank = constraintAnnotation.blank();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        if (value.isBlank()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(this.blank).addConstraintViolation();
            return false;
        }

        int minUsername = 5;
        int maxUsername = 20;
        int minEmail = 7;
        int maxEmail = 60;
        int length = value.length();
        boolean isValid;

        // validate email
        if (value.contains("@")) {
            if (length < minEmail || length > maxEmail) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(UserValidationMessage.EMAIL_LENGTH_RANGE.getName())
                        .addConstraintViolation();
                return false;
            }
            isValid = userValidationUtil.isValidEmail(value);
            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(this.emailMessage).addConstraintViolation();
                return false;
            }
        }
        // validate username
        else {
            if (length < minUsername || length > maxUsername) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(UserValidationMessage.USERNAME_LENGTH_RANGE.getName())
                        .addConstraintViolation();
                return false;
            }
            isValid = userValidationUtil.isValidUsername(value);
            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(this.usernameMessage)
                        .addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
