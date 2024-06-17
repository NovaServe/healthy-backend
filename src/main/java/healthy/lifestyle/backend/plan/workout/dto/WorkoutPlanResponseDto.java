package healthy.lifestyle.backend.plan.workout.dto;

import java.time.LocalDate;
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

    private String workoutTitle;

    private LocalDate startDate; // in user's zone

    private LocalDate endDate; // in user's zone

    private String jsonDescription;

    private LocalDateTime createdAt; // in user's zone
}
