package healthy.lifestyle.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IdValidator implements ConstraintValidator<IdValidation, Long> {
    @Override
    public void initialize(IdValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        return validation(value);
    }

    public boolean validation(Long id) {
        if (id == null) return false;
        return id >= 0;
    }
}
