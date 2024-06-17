package healthy.lifestyle.backend.plan.workout.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutWithoutPlanResponseDto {
    private Long id;

    private String title;
}
