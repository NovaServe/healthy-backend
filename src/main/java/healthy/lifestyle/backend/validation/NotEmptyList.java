package healthy.lifestyle.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotEmptyListValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmptyList {
    String message() default "Should be not empty list";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
