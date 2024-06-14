package healthy.lifestyle.backend.user.dto;

import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import healthy.lifestyle.backend.user.validation.annotation.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldsValuesMatchValidation.List({
    @FieldsValuesMatchValidation(
            field = "password",
            fieldMatch = "confirmPassword",
            message = "{validation.message.passwords-mismatch}")
})
public class SignupRequestDto {
    @UsernameValidation
    private String username;

    @EmailValidation
    private String email;

    @FullNameValidation
    private String fullName;

    @IdValidation
    private Long countryId;

    @IdValidation
    private Long timezoneId;

    @AgeOptionalValidation
    private Integer age;

    @PasswordValidation
    private String password;

    @PasswordValidation
    private String confirmPassword;
}
