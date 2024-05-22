package healthy.lifestyle.backend.plan.workout.dto;

import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanCreateRequestDto {
    @IdValidation
    private Long workoutId;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String jsonDescription;
}
