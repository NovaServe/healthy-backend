package healthy.lifestyle.backend.reminder.workout.dto;

import healthy.lifestyle.backend.calendar.dto.DayRequestDto;
import healthy.lifestyle.backend.calendar.model.ReminderType;
import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutReminderCreateRequestDto {

    @IdValidation
    private Long workoutId;

    @NotNull private ReminderType reminderType;

    @NotNull @DateTimeFormat
    private LocalDate startDate;

    @DateTimeFormat
    private LocalDate endDate;

    private Integer applyDays;

    Integer notifyBeforeInMinutes;

    List<DayRequestDto> days;
}
