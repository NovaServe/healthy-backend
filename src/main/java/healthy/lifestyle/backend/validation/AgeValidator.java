package healthy.lifestyle.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AgeValidator implements ConstraintValidator<AgeValidation, Integer> {
    @Override
    public void initialize(AgeValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return validation(value);
    }

    public boolean validation(Integer input) {
        if (input != null) return input >= 16 && input <= 120;
        return true;
    }
}
