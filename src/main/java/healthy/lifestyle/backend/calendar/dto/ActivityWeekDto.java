package healthy.lifestyle.backend.calendar.dto;

import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityWeekDto {

    LocalDate weekStartDate;

    LocalDate weekEndDate;

    private ActivityDayDto monday;

    private ActivityDayDto tuesday;

    private ActivityDayDto wednesday;

    private ActivityDayDto thursday;

    private ActivityDayDto friday;

    private ActivityDayDto saturday;

    private ActivityDayDto sunday;
}
