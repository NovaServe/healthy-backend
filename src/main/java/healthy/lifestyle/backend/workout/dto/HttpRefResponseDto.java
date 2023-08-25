package healthy.lifestyle.backend.workout.dto;

public class HttpRefResponseDto {
    private long id;
    private String name;
    private String description;
    private String ref;
    private boolean isCustom;

    public HttpRefResponseDto() {}

    public HttpRefResponseDto(long id, String name, String description, String ref, boolean isCustom) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ref = ref;
        this.isCustom = isCustom;
    }

    public HttpRefResponseDto(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.ref = builder.ref;
        this.isCustom = builder.isCustom;
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

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public static class Builder {
        private long id;
        private String name;
        private String description;
        private String ref;
        private boolean isCustom;

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

        public Builder isCustom(boolean isCustom) {
            this.isCustom = isCustom;
            return this;
        }

        public HttpRefResponseDto build() {
            return new HttpRefResponseDto(this);
        }
    }
}
