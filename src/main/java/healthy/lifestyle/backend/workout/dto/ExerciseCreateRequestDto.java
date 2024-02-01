package healthy.lifestyle.backend.workout.dto;

import healthy.lifestyle.backend.validation.DescriptionValidation;
import healthy.lifestyle.backend.validation.NotEmptyList;
import healthy.lifestyle.backend.validation.TitleValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseCreateRequestDto {
    @TitleValidation
    @NotBlank(message = "Title should contain at least 5 characters")
    @Size(min = 5, max = 255, message = "Available size is 5 to 255 chars")
    private String title;

    @Size(max = 255, message = "Max size is 255 chars")
    @DescriptionValidation
    private String description;

    @NotNull private boolean needsEquipment;

    @NotNull @NotEmptyList
    private List<Long> bodyParts;

    private List<Long> httpRefs;
}
