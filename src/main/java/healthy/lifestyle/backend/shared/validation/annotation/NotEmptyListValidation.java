package healthy.lifestyle.backend.shared.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotEmptyListValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmptyListValidation {
    String message() default "{validation.message.empty-list}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
