package healthy.lifestyle.backend.shared.validation.annotation;

import healthy.lifestyle.backend.shared.validation.ValidationUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotEmptyListValidator implements ConstraintValidator<NotEmptyListValidation, List<Long>> {
    @Autowired
    ValidationUtil validationUtil;

    @Override
    public void initialize(NotEmptyListValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<Long> value, ConstraintValidatorContext context) {
        return validationUtil.isNotEmptyList(value);
    }
}
