package healthy.lifestyle.backend.user.dto;

import healthy.lifestyle.backend.validation.PasswordValidation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    @NotBlank
    @Size(min = 6, max = 40)
    @Email
    private String usernameOrEmail;

    @NotBlank
    @Size(min = 10, max = 64)
    @PasswordValidation
    private String password;
}
