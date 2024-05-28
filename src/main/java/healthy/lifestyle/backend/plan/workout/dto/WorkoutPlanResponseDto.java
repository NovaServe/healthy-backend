package healthy.lifestyle.backend.plan.workout.dto;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanResponseDto {

    private Long id;

    private Long workoutId;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String jsonDescription;

    private LocalDateTime createdAt;

}
