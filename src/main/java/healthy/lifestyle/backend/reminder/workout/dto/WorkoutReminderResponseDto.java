package healthy.lifestyle.backend.reminder.workout.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutReminderResponseDto {
    private long id;

    private long workoutId;

    private String workoutTitle;

    private String xmlDescription;

    @JsonProperty(value = "isActive")
    private boolean isActive;

    @JsonProperty(value = "isPaused")
    private boolean isPaused;

    private LocalDate createdAt;
}
