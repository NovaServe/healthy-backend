package healthy.lifestyle.backend.reminder.workout.dto;

import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutReminderCreateRequestDto {
    @IdValidation
    private Long workoutId;

    private String xml_description;
}
