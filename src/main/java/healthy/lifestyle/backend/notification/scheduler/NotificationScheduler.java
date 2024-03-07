package healthy.lifestyle.backend.notification.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    @Autowired
    NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void addScheduledFuture() {
        notificationService.clearDoneTasks();
        logger.info("Every-hour scheduler has been started");

        //        List<TaskDto> taskDtoList = new ArrayList<>();
        //        notificationService.addScheduledFuture(taskDtoList);
    }

    //    @Scheduled(fixedDelay = 5000)
    //    public void testNotification() {
    //    }
}
