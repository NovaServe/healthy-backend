package healthy.lifestyle.backend.workout.dto;

import healthy.lifestyle.backend.validation.DescriptionValidation;
import healthy.lifestyle.backend.validation.TitleValidation;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CreateWorkoutRequestDto {
    @TitleValidation
    private String title;

    @DescriptionValidation
    private String description;

    @NotEmpty
    private List<Long> exerciseIds;
}
