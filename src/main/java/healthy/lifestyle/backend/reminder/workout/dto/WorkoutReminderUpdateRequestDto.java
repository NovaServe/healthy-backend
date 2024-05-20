package healthy.lifestyle.backend.reminder.workout.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutReminderUpdateRequestDto {
    private String jsonDescription;
}
