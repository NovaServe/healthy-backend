package healthy.lifestyle.backend.reminder.workout.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String jsonDescription;

    @JsonProperty(value = "isActive")
    private boolean isActive;

    private Timestamp createdAt;

    private Timestamp deactivatedAt;
}
