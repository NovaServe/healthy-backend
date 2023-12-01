package healthy.lifestyle.backend.users.dto;

import healthy.lifestyle.backend.validation.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldsValueMatch.List({
    @FieldsValueMatch(
            field = "updatedPassword",
            fieldMatch = "updatedConfirmPassword",
            message = "Passwords don't match")
})
public class UserUpdateRequestDto {
    @Size(min = 6, max = 20)
    @UsernameValidation
    private String updatedUsername;

    @Email
    @Size(min = 6, max = 64)
    @EmailValidation
    private String updatedEmail;

    @Size(min = 10, max = 64)
    @PasswordValidation
    private String updatedPassword;

    @Size(min = 10, max = 64)
    @PasswordValidation
    private String updatedConfirmPassword;

    @Size(min = 4, max = 64)
    @FullnameValidation
    private String updatedFullName;

    private Long updatedCountryId;

    @AgeValidation
    private Integer updatedAge;
}
