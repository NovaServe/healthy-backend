package healthy.lifestyle.backend.workout.dto;

import healthy.lifestyle.backend.validation.DescriptionValidation;
import healthy.lifestyle.backend.validation.TitleValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class CreateExerciseRequestDto {
    @NotBlank(message = "Title should contain at least 2 characters")
    @Size(min = 2, max = 255, message = "Available size is 2 to 255 chars")
    @TitleValidation
    private String title;

    @Size(max = 255, message = "Max size is 255 chars")
    @DescriptionValidation
    private String description;

    @Size(max = 255, message = "Max size is 255 chars")
    @NotNull private List<BodyPartRequestDto> bodyParts;

    @Size(max = 2000, message = "Max size is 2000 chars")
    private List<HttpRefRequestDto> httpRefs;

    public CreateExerciseRequestDto() {}

    public CreateExerciseRequestDto(
            String title, String description, List<BodyPartRequestDto> bodyParts, List<HttpRefRequestDto> httpRefs) {
        this.title = title;
        this.description = description;
        this.bodyParts = bodyParts;
        this.httpRefs = httpRefs;
    }

    public CreateExerciseRequestDto(Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.bodyParts = builder.bodyParts;
        this.httpRefs = builder.httpRefs;
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

    public List<BodyPartRequestDto> getBodyParts() {
        return bodyParts;
    }

    public void setBodyParts(List<BodyPartRequestDto> bodyParts) {
        this.bodyParts = bodyParts;
    }

    public List<HttpRefRequestDto> getHttpRefs() {
        return httpRefs;
    }

    public void setHttpRefs(List<HttpRefRequestDto> httpRefs) {
        this.httpRefs = httpRefs;
    }

    public static class Builder {
        private String title;
        private String description;
        private List<BodyPartRequestDto> bodyParts;
        private List<HttpRefRequestDto> httpRefs;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder bodyParts(List<BodyPartRequestDto> bodyParts) {
            this.bodyParts = bodyParts;
            return this;
        }

        public Builder httpRefs(List<HttpRefRequestDto> httpRefs) {
            this.httpRefs = httpRefs;
            return this;
        }

        public CreateExerciseRequestDto build() {
            return new CreateExerciseRequestDto(this);
        }
    }
}
