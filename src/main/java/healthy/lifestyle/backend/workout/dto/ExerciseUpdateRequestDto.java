package healthy.lifestyle.backend.workout.dto;

import healthy.lifestyle.backend.validation.DescriptionValidation;
import healthy.lifestyle.backend.validation.NotEmptyList;
import healthy.lifestyle.backend.validation.TitleValidation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseUpdateRequestDto {
    @TitleValidation
    @Size(min = 5, max = 255, message = "Available size is 5 to 255 chars")
    private String title;

    @DescriptionValidation
    private String description;

    private Boolean needsEquipment;

    @NotNull private List<Long> httpRefIds;

    @NotNull @NotEmptyList
    private List<Long> bodyPartIds;
}
