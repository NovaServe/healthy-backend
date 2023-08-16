package healthy.lifestyle.backend.users.dto;

public class LoginResponseDto {
    private String token;

    public LoginResponseDto() {}

    public LoginResponseDto(String token) {
        this.token = token;
    }

    public LoginResponseDto(Builder builder) {
        this.token = builder.token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static class Builder {
        private String token;

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public LoginResponseDto build() {
            return new LoginResponseDto(this);
        }
    }
}
