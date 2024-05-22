package healthy.lifestyle.backend.plan.workout.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanUpdateRequestDto {
    private String jsonDescription;
}
