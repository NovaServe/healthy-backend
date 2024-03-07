package healthy.lifestyle.backend.activity.workout.dto;

import healthy.lifestyle.backend.shared.validation.annotation.DescriptionOptionalValidation;
import healthy.lifestyle.backend.shared.validation.annotation.TitleOptionalValidation;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseUpdateRequestDto {
    @TitleOptionalValidation
    private String title;

    @DescriptionOptionalValidation
    private String description;

    private Boolean needsEquipment;

    private List<Long> httpRefIds;

    @NotEmpty
    private List<Long> bodyPartIds;
}
