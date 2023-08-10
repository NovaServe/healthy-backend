package healthy.lifestyle.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FullnameValidator implements ConstraintValidator<FullnameValidation, String> {
    @Override
    public void initialize(FullnameValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return validation(value);
    }

    public boolean validation(String input) {
        String trim = input.trim();
        char[] notAllowed = new char[] {
            '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '+', '=', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', ',', '<', '.', '>', '?', '\\', '/', '`', '~'
        };
        for (char ch : notAllowed) {
            if (trim.indexOf(ch) != -1) return false;
        }
        return true;
    }
}
