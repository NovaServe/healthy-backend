package healthy.lifestyle.backend.user.dto;

import healthy.lifestyle.backend.user.validation.annotation.PasswordValidation;
import healthy.lifestyle.backend.user.validation.annotation.UsernameOrEmailValidation;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    @UsernameOrEmailValidation
    private String usernameOrEmail;

    @PasswordValidation
    private String password;
}
