package healthy.lifestyle.backend.user.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UsernameOrEmailValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UsernameOrEmailValidation {
    String message() default "{validation.message.not-null}";

    String blank() default "{validation.message.not-blank}";

    String usernameMessage() default "{validation.message.username.allowed-symbols}";

    String emailMessage() default "{validation.message.email.allowed-symbols}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
