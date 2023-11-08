package healthy.lifestyle.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = HttpValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpValidation {
    String message() default "Invalid format, should start with http";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
