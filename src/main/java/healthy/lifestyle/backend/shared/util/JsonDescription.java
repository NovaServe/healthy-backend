package healthy.lifestyle.backend.shared.util;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.DayOfWeek;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class JsonDescription {
    private long json_id;
    private DayOfWeek dayOfWeek;
    private int hours;
    private int minutes;
}
