package healthy.lifestyle.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class NotEmptyListValidator implements ConstraintValidator<NotEmptyList, List<Long>> {
    @Override
    public void initialize(NotEmptyList constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<Long> value, ConstraintValidatorContext context) {
        return validation(value);
    }

    public boolean validation(List<Long> input) {
        if (input != null) return !input.isEmpty();
        return true;
    }
}
