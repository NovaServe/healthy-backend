package healthy.lifestyle.backend.validation;

import static java.util.Objects.nonNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
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
        if (nonNull(input)) {
            return input >= 5 && input <= 200;
        }
        return true;
    }
}
