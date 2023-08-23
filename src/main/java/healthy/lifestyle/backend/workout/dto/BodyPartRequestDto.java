package healthy.lifestyle.backend.workout.dto;

import healthy.lifestyle.backend.validation.IdValidation;
import jakarta.validation.constraints.NotNull;

public class BodyPartRequestDto {
    @NotNull @IdValidation
    private long id;

    public BodyPartRequestDto() {}

    public BodyPartRequestDto(long id) {
        this.id = id;
    }

    public BodyPartRequestDto(Builder builder) {
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

        public BodyPartRequestDto build() {
            return new BodyPartRequestDto(this);
        }
    }
}
