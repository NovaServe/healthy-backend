package healthy.lifestyle.backend.calendar.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDayDto {

    private LocalDate dayDate;

    private DayOfWeek dayOfWeek;

    private List<ActivityRowDto> activities;
}
