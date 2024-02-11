package healthy.lifestyle.backend.shared.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TitleValidator.class)
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TitleValidation {
    String message() default "{validation.message.title.allowed-symbols}";

    int min() default 5;

    int max() default 255;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
