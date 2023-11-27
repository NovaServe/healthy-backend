package healthy.lifestyle.backend.workout.dto;

import healthy.lifestyle.backend.validation.DescriptionValidation;
import healthy.lifestyle.backend.validation.TitleValidation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ExerciseUpdateRequestDto {
    @TitleValidation
    @Size(min = 5, max = 255, message = "Size should be from 5 to 255 characters long")
    private String title;

    @DescriptionValidation
    private String description;

    private Boolean needsEquipment;

    @NotNull private List<Long> httpRefIds;

    @NotNull private List<Long> bodyPartIds;
}
