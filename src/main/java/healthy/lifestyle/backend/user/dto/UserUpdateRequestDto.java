package healthy.lifestyle.backend.user.dto;

import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import healthy.lifestyle.backend.user.validation.annotation.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldsValuesMatchOptionalValidation.List({
    @FieldsValuesMatchOptionalValidation(
            field = "password",
            fieldMatch = "confirmPassword",
            message = "{validation.message.passwords-mismatch}")
})
public class UserUpdateRequestDto {
    @UsernameOptionalValidation
    private String username;

    @EmailOptionalValidation
    private String email;

    @FullNameOptionalValidation
    private String fullName;

    @IdValidation
    private Long countryId;

    @AgeOptionalValidation
    private Integer age;

    @PasswordOptionalValidation
    private String password;

    @PasswordOptionalValidation
    private String confirmPassword;
}
