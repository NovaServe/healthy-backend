package healthy.lifestyle.backend.users.dto;

import healthy.lifestyle.backend.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldsValueMatch.List({
    @FieldsValueMatch(field = "password", fieldMatch = "confirmPassword", message = "Passwords don't match")
})
public class SignupRequestDto {
    @NotBlank
    @Size(min = 6, max = 20)
    @UsernameValidation
    private String username;

    @NotBlank
    @Email
    @Size(min = 6, max = 64)
    private String email;

    @NotBlank
    @Size(min = 10, max = 64)
    @PasswordValidation
    private String password;

    @NotBlank
    @Size(min = 10, max = 64)
    @PasswordValidation
    private String confirmPassword;

    @NotBlank
    @Size(min = 4, max = 64)
    @FullNameValidation
    private String fullName;

    @NotNull @PositiveOrZero
    private Long countryId;

    @AgeValidation
    private Integer age;
}
