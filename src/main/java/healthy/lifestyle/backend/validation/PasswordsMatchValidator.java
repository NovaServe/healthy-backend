package healthy.lifestyle.backend.validation;

import static java.util.Objects.isNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatchValidation, Object> {

    private String updatedPassword;
    private String updatedConfirmPassword;

    @Override
    public void initialize(PasswordsMatchValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.updatedPassword = constraintAnnotation.password();
        this.updatedConfirmPassword = constraintAnnotation.confirmPassword();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object passwordValue = new BeanWrapperImpl(value).getPropertyValue(updatedPassword);
        Object confirmPasswordValue = new BeanWrapperImpl(value).getPropertyValue(updatedConfirmPassword);
        if (isNull(passwordValue) && isNull(confirmPasswordValue)) {
            return true;
        }
        return passwordValue != null && passwordValue.equals(confirmPasswordValue);
    }
}
