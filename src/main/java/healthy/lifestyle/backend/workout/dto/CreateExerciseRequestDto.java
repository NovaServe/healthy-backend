package healthy.lifestyle.backend.workout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public class CreateExerciseRequestDto {
    @NotBlank(message = "Title should contain at least 2 characters")
    @Size(min = 2, max = 255, message = "Available size is 2 to 255 chars")
    private String title;

    @Size(max = 255, message = "Max size is 255 chars")
    private String description;

    @Size(max = 255, message = "Max size is 255 chars")
    @NotNull private Set<BodyPartRequestDto> bodyParts;

    @Size(max = 2000, message = "Max size is 2000 chars")
    private Set<HttpRefRequestDto> httpRefs;

    public CreateExerciseRequestDto() {}

    public CreateExerciseRequestDto(
            String title, String description, Set<BodyPartRequestDto> bodyParts, Set<HttpRefRequestDto> httpRefs) {
        this.title = title;
        this.description = description;
        this.bodyParts = bodyParts;
        this.httpRefs = httpRefs;
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

    public Set<BodyPartRequestDto> getBodyParts() {
        return bodyParts;
    }

    public void setBodyParts(Set<BodyPartRequestDto> bodyParts) {
        this.bodyParts = bodyParts;
    }

    public Set<HttpRefRequestDto> getHttpRefs() {
        return httpRefs;
    }

    public void setHttpRefs(Set<HttpRefRequestDto> httpRefs) {
        this.httpRefs = httpRefs;
    }
}
