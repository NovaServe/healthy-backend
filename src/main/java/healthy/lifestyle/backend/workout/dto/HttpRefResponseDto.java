package healthy.lifestyle.backend.workout.dto;

public class HttpRefResponseDto {
    private long id;
    private String name;
    private String description;
    private String ref;

    public HttpRefResponseDto() {}

    public HttpRefResponseDto(long id, String name, String description, String ref) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ref = ref;
    }

    public HttpRefResponseDto(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.ref = builder.ref;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public static class Builder {
        private long id;
        private String name;
        private String description;
        private String ref;

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public HttpRefResponseDto build() {
            return new HttpRefResponseDto(this);
        }
    }
}
