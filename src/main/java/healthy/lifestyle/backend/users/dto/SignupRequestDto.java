package healthy.lifestyle.backend.users.dto;

import healthy.lifestyle.backend.validation.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

    public SignupRequestDto() {}

    public SignupRequestDto(
            String username, String email, String password, String confirmPassword, String fullName, Long countryId) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.fullName = fullName;
        this.countryId = countryId;
    }

    public SignupRequestDto(Builder builder) {
        this.username = builder.username;
        this.email = builder.email;
        this.password = builder.password;
        this.confirmPassword = builder.confirmPassword;
        this.fullName = builder.fullName;
        this.countryId = builder.countryId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password.trim();
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword.trim();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName.trim();
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public static class Builder {
        private String username;
        private String email;
        private String password;
        private String confirmPassword;
        private String fullName;
        private Long countryId;

        public Builder username(String username) {
            this.username = username.trim();
            return this;
        }

        public Builder email(String email) {
            this.email = email.trim();
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

        public Builder fullName(String fullName) {
            this.fullName = fullName.trim();
            return this;
        }

        public Builder country(Long countryId) {
            this.countryId = countryId;
            return this;
        }

        public SignupRequestDto build() {
            return new SignupRequestDto(this);
        }
    }
}
