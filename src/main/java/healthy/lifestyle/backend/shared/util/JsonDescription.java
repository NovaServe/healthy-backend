package healthy.lifestyle.backend.shared.util;

import java.time.DayOfWeek;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsonDescription {
    private long json_id;
    private DayOfWeek dayOfWeek;
    private int hours;
    private int minutes;
}
