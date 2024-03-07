package healthy.lifestyle.backend.calendar.dto;

import java.time.DayOfWeek;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayRequestDto {

    private DayOfWeek day;

    // [0; 23]
    private int hours;

    // [0; 59]
    private int minutes;
}
