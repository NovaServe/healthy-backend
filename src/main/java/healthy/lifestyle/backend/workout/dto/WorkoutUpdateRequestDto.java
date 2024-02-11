package healthy.lifestyle.backend.workout.dto;

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
public class WorkoutUpdateRequestDto {
    @TitleOptionalValidation
    private String title;

    @DescriptionOptionalValidation
    private String description;

    @NotEmpty
    private List<Long> exerciseIds;
}
