package healthy.lifestyle.backend.user.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AgeOptionalValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AgeOptionalValidation {
    String message() default "{validation.message.age.size-range}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int min() default 16;

    int max() default 120;
}
