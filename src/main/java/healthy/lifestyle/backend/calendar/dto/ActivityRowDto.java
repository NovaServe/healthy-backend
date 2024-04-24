package healthy.lifestyle.backend.calendar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import healthy.lifestyle.backend.calendar.model.ActivityType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRowDto {

    private long activityId;

    private ActivityType activityType;

    @JsonProperty(value = "isSingle")
    private boolean isSingle;

    @JsonProperty(value = "isPausedBilling")
    private boolean isPausedBilling;

    @JsonProperty(value = "isCompleted")
    private boolean isCompleted;

    private String title;

    private int hours;

    private int minutes;
}
