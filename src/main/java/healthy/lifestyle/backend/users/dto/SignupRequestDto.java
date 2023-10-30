package healthy.lifestyle.backend.users.dto;

import healthy.lifestyle.backend.validation.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @EmailValidation
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
    @FullnameValidation
    private String fullName;

    @NotNull private Long countryId;

    @AgeValidation
    private Integer age;
}
