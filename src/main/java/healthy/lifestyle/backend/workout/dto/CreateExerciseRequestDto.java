package healthy.lifestyle.backend.workout.dto;

import healthy.lifestyle.backend.validation.DescriptionValidation;
import healthy.lifestyle.backend.validation.TitleValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CreateExerciseRequestDto {
    @NotBlank(message = "Title should contain at least 2 characters")
    @Size(min = 2, max = 255, message = "Available size is 2 to 255 chars")
    @TitleValidation
    private String title;

    @Size(max = 255, message = "Max size is 255 chars")
    @DescriptionValidation
    private String description;

    @NotNull private boolean needsEquipment;

    @NotNull private List<Long> bodyParts;

    private List<Long> httpRefs;
}
