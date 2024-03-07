package healthy.lifestyle.backend.reminder.workout.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import healthy.lifestyle.backend.calendar.dto.DayResponseDto;
import healthy.lifestyle.backend.calendar.model.ReminderType;
import java.time.LocalDate;
import java.util.List;
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

    private ReminderType reminderType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer applyDays;

    @JsonProperty(value = "isActive")
    private boolean isActive;

    @JsonProperty(value = "isPaused")
    private boolean isPaused;

    private LocalDate pauseStartDate;

    private LocalDate pauseEndDate;

    @JsonProperty(value = "isPausedBilling")
    private boolean isPausedBilling;

    private Integer notifyBeforeInMinutes;

    private LocalDate createdAt;

    private List<DayResponseDto> days;
}
