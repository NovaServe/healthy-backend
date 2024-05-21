package healthy.lifestyle.backend.reminder.workout.dto;

import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutReminderCreateRequestDto {
    @IdValidation
    private Long workoutId;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String jsonDescription;
}
