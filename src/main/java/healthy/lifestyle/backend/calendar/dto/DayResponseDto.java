package healthy.lifestyle.backend.calendar.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayResponseDto {

    private DayOfWeek day;

    // [0; 23]
    private int hours;

    // [0; 59]
    private int minutes;

    private int hoursNotifyBefore;

    private int minutesNotifyBefore;

    private int hoursNotifyBeforeDefault;

    private int minutesNotifyBeforeDefault;

    private boolean isActive;

    private LocalDate createdAt;

    private LocalDate deactivatedAt;
}
