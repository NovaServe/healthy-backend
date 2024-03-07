package healthy.lifestyle.backend.calendar.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityMonthDto {

    LocalDate monthStartDate;

    LocalDate monthEndDate;

    List<ActivityWeekDto> weeks;
}
