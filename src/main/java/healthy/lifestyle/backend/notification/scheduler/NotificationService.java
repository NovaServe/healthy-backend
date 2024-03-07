package healthy.lifestyle.backend.notification.scheduler;

import healthy.lifestyle.backend.notification.firebase.FirebaseMessageDto;
import healthy.lifestyle.backend.notification.firebase.FirebaseMessagingService;
import healthy.lifestyle.backend.notification.kafka.KafkaProducer;
import healthy.lifestyle.backend.notification.shared.ActivityType;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    // public class NotificationService implements SchedulingConfigurer {

    @Autowired
    TaskScheduler taskScheduler;

    @Autowired
    KafkaProducer kafkaProducer;

    @Autowired
    FirebaseMessagingService firebaseMessagingService;

    private final Map<TaskDto, ScheduledFuture> taskMapping = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    // private ScheduledTaskRegistrar scheduledTaskRegistrar;

    //    @Override
    //    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    //        taskRegistrar.setTaskScheduler(taskScheduler);
    //        scheduledTaskRegistrar = taskRegistrar;
    //    }

    public void addScheduledFuture(List<TaskDto> taskDtoList) {

        for (TaskDto taskDto : taskDtoList) {
            FirebaseMessageDto firebaseMessageDto = FirebaseMessageDto.buildFromTaskDto(taskDto);
            Runnable runnable = () -> {
                try {
                    // firebaseMessagingService.sendNotificationByToken(firebaseMessageDto);
                    kafkaProducer.sendMessage(taskDto);
                } catch (Exception e) {
                    logger.error("Error occurred while sending notification: {}", e.getMessage());
                }
            };

            ScheduledFuture<?> scheduledFuture =
                    taskScheduler.schedule(runnable, Instant.from(taskDto.getNotificationStartDateTimeInServerZone()));

            taskMapping.put(taskDto, scheduledFuture);

            logger.info(
                    "Event has been scheduled to send {} with title: {}, body: {}",
                    taskDto.getNotificationStartDateTimeInServerZone(),
                    firebaseMessageDto.getTitle(),
                    firebaseMessageDto.getBody());
        }
    }

    public void addScheduledFuture(TaskDto taskDto) {
        addScheduledFuture(List.of(taskDto));
    }

    public void cancelScheduledFuture(ActivityType activityType, long activityId, long reminderId) {
        Iterator<Map.Entry<TaskDto, ScheduledFuture>> iterator =
                taskMapping.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<TaskDto, ScheduledFuture> entry = iterator.next();
            TaskDto taskDto = entry.getKey();
            ScheduledFuture scheduledFuture = entry.getValue();

            if (taskDto.getActivityType().equals(activityType)
                    && taskDto.getActivityId() == activityId
                    && taskDto.getReminderId() == reminderId) {
                scheduledFuture.cancel(true);
                iterator.remove();
            }
        }
    }

    public void clearDoneTasks() {
        Iterator<Map.Entry<TaskDto, ScheduledFuture>> iterator =
                taskMapping.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<TaskDto, ScheduledFuture> entry = iterator.next();
            ScheduledFuture scheduledFuture = entry.getValue();

            if (scheduledFuture.isDone()) {
                scheduledFuture.cancel(false);
                iterator.remove();
            }
        }
    }

    private void clearTasks(boolean mayInterruptIfRunning) {
        taskMapping.forEach((taskDto, scheduledFuture) -> scheduledFuture.cancel(mayInterruptIfRunning));
    }
}
