package healthy.lifestyle.backend.notification.scheduler;

import healthy.lifestyle.backend.notification.shared.ActivityType;
import healthy.lifestyle.backend.notification.shared.NotificationType;
import java.time.ZonedDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private long userId;

    private NotificationType notificationType;

    private int notifyBeforeInMinutes;

    private ActivityType activityType;

    private long activityId;

    private long planId;

    private ZonedDateTime notificationStartDateTimeInServerZone;

    private ZonedDateTime activityStartDateTimeInUserZone;
}
