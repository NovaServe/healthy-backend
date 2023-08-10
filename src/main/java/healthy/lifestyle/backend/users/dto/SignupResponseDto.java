package healthy.lifestyle.backend.users.dto;

public class SignupResponseDto {
    private long id;

    public SignupResponseDto() {}

    public SignupResponseDto(long id) {
        this.id = id;
    }

    public SignupResponseDto(Builder builder) {
        this.id = builder.id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public static class Builder {
        private long id;

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public SignupResponseDto build() {
            return new SignupResponseDto(this);
        }
    }
}
