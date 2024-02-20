package healthy.lifestyle.backend.shared.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = WebLinkValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface WebLinkValidation {
    String message() default "{validation.message.web-link}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
