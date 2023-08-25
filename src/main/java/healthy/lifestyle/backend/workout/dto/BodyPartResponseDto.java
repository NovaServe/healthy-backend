package healthy.lifestyle.backend.workout.dto;

public class BodyPartResponseDto {
    private long id;

    private String name;

    public BodyPartResponseDto() {}

    public BodyPartResponseDto(long id, String name) {
        this.id = id;
        this.name = name;
    }

    private BodyPartResponseDto(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class Builder {
        private long id;

        private String name;

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public BodyPartResponseDto build() {
            return new BodyPartResponseDto(this);
        }
    }
}
