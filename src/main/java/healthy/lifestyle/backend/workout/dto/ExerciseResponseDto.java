package healthy.lifestyle.backend.workout.dto;

import java.util.Set;

public class ExerciseResponseDto {
    private Long id;

    private String title;

    private String description;

    private Set<BodyPartResponseDto> bodyParts;

    private Set<HttpRefResponseDto> httpRefs;

    public ExerciseResponseDto(
            Long id,
            String title,
            String description,
            Set<BodyPartResponseDto> bodyParts,
            Set<HttpRefResponseDto> httpRefs) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.bodyParts = bodyParts;
        this.httpRefs = httpRefs;
    }

    public ExerciseResponseDto(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.bodyParts = builder.bodyParts;
        this.httpRefs = builder.httpRefs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<BodyPartResponseDto> getBodyParts() {
        return bodyParts;
    }

    public void setBodyParts(Set<BodyPartResponseDto> bodyParts) {
        this.bodyParts = bodyParts;
    }

    public Set<HttpRefResponseDto> getHttpRefs() {
        return httpRefs;
    }

    public void setHttpRefs(Set<HttpRefResponseDto> httpRefs) {
        this.httpRefs = httpRefs;
    }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private Set<BodyPartResponseDto> bodyParts;
        private Set<HttpRefResponseDto> httpRefs;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder bodyParts(Set<BodyPartResponseDto> bodyParts) {
            this.bodyParts = bodyParts;
            return this;
        }

        public Builder httpRefs(Set<HttpRefResponseDto> httpRefs) {
            this.httpRefs = httpRefs;
            return this;
        }

        public ExerciseResponseDto build() {
            return new ExerciseResponseDto(this);
        }
    }
}
