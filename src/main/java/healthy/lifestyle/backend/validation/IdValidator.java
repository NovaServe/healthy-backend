package healthy.lifestyle.backend.validation;

import static java.util.Objects.isNull;

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
        if (isNull(id)) return false;
        return id >= 0;
    }
}
