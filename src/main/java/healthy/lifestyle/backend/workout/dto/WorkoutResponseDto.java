package healthy.lifestyle.backend.workout.dto;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class WorkoutResponseDto {
    private Long id;

    private String title;

    private String description;

    private boolean isCustom;

    private List<BodyPartResponseDto> bodyParts;

    private boolean needsEquipment;

    private List<ExerciseResponseDto> exercises;
}
