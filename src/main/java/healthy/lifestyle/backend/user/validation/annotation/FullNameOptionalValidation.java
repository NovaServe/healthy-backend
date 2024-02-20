package healthy.lifestyle.backend.user.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FullNameOptionalValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FullNameOptionalValidation {
    String message() default "{validation.message.full-name.allowed-symbols}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
