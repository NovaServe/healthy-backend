package healthy.lifestyle.backend.users.dto;

import healthy.lifestyle.backend.validation.EmailValidation;
import healthy.lifestyle.backend.validation.FieldsValueMatch;
import healthy.lifestyle.backend.validation.PasswordValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@FieldsValueMatch.List({
    @FieldsValueMatch(field = "password", fieldMatch = "confirmPassword", message = "Passwords don't match")
})
public class LoginRequestDto {
    @NotBlank
    @Size(min = 6, max = 40)
    @EmailValidation
    private String usernameOrEmail;

    @NotBlank
    @Size(min = 10, max = 64)
    @PasswordValidation
    private String password;

    @NotBlank
    @Size(min = 10, max = 64)
    @PasswordValidation
    private String confirmPassword;

    public LoginRequestDto() {}

    public LoginRequestDto(String usernameOrEmail, String password, String confirmPassword) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public LoginRequestDto(Builder builder) {
        this.usernameOrEmail = builder.usernameOrEmail;
        this.password = builder.password;
        this.confirmPassword = builder.confirmPassword;
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public static class Builder {
        private String usernameOrEmail;
        private String password;
        private String confirmPassword;

        public Builder usernameOrEmail(String usernameOrEmail) {
            this.usernameOrEmail = usernameOrEmail.trim();
            return this;
        }

        public Builder password(String password) {
            this.password = password.trim();
            return this;
        }

        public Builder confirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword.trim();
            return this;
        }

        public LoginRequestDto build() {
            return new LoginRequestDto(this);
        }
    }
}
