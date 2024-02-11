package healthy.lifestyle.backend.user.validation.annotation;

import healthy.lifestyle.backend.user.validation.UserValidationUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AgeOptionalValidator implements ConstraintValidator<AgeOptionalValidation, Integer> {
    @Autowired
    UserValidationUtil userValidationUtil;

    @Override
    public void initialize(AgeOptionalValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null values are considered valid
        }
        return userValidationUtil.isValidAge(value);
    }
}
