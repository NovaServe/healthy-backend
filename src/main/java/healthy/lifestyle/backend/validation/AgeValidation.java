package healthy.lifestyle.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AgeValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AgeValidation {
    String message() default "Not allowed age, should be in 5-200";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
