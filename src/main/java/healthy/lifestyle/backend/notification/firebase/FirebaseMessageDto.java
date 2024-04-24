package healthy.lifestyle.backend.notification.firebase;

import healthy.lifestyle.backend.notification.scheduler.TaskDto;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseMessageDto {
    private String title;

    private String body;

    private String image;

    public static FirebaseMessageDto buildFromTaskDto(TaskDto taskDto) {
        String title = "";
        switch (taskDto.getNotificationType()) {
            case MAIN -> title = taskDto.getActivityType().getValue() + " begins";
            case BEFORE -> title = taskDto.getNotifyBeforeInMinutes() + " minutes";
            case DEFAULT -> title = "5 minutes";
        }

        String message = "";
        switch (taskDto.getNotificationType()) {
            case BEFORE, DEFAULT -> {
                String minutes = "";
                if (taskDto.getActivityStartDateTimeInUserZone().getMinute() >= 0
                        && taskDto.getActivityStartDateTimeInUserZone().getMinute() <= 9) {
                    minutes = "0" + taskDto.getActivityStartDateTimeInUserZone().getMinute();
                } else {
                    minutes = String.valueOf(
                            taskDto.getActivityStartDateTimeInUserZone().getMinute());
                }
                message = String.format(
                        "%s starts at %d:%s",
                        taskDto.getActivityType().getValue(),
                        taskDto.getActivityStartDateTimeInUserZone().getHour(),
                        minutes);
            }
        }

        return FirebaseMessageDto.builder().title(title).body(message).build();
    }
}
