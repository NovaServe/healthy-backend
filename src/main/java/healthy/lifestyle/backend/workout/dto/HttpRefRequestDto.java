package healthy.lifestyle.backend.workout.dto;

import jakarta.validation.constraints.NotNull;

public class HttpRefRequestDto {
    @NotNull private long id;

    public HttpRefRequestDto() {}

    public HttpRefRequestDto(long id) {
        this.id = id;
    }

    public HttpRefRequestDto(Builder builder) {
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

        public HttpRefRequestDto build() {
            return new HttpRefRequestDto(this);
        }
    }
}
