package healthy.lifestyle.backend.validation;

import static java.util.Objects.isNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DescriptionValidator implements ConstraintValidator<DescriptionValidation, String> {
    @Override
    public void initialize(DescriptionValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return validation(value);
    }

    public boolean validation(String input) {
        if (isNull(input)) return true;

        String trim = input.trim();
        char[] notAllowed =
                new char[] {'!', '@', '#', '$', '%', '^', '&', '*', '+', '=', '<', '>', '?', '\\', '/', '`', '~'};
        for (char ch : notAllowed) {
            if (trim.indexOf(ch) != -1) return false;
        }
        return true;
    }
}
