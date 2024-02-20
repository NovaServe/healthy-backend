package healthy.lifestyle.backend.shared.validation.annotation;

import healthy.lifestyle.backend.shared.validation.ValidationMessage;
import healthy.lifestyle.backend.shared.validation.ValidationUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TitleValidator implements ConstraintValidator<TitleValidation, String> {
    @Autowired
    ValidationUtil validationUtil;

    private int min;

    private int max;

    @Override
    public void initialize(TitleValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank() || value.length() < this.min || value.length() > this.max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            String.format(ValidationMessage.LENGTH_RANGE.getName(), this.min, this.max))
                    .addConstraintViolation();
            return false;
        }
        return validationUtil.isValidTitle(value);
    }
}
