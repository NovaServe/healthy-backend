package healthy.lifestyle.backend.notification.scheduler;

import java.time.Clock;
import java.util.TimeZone;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);

        Clock clock = Clock.system(TimeZone.getTimeZone("Europe/London").toZoneId());
        taskScheduler.setClock(clock);

        return taskScheduler;
    }
}
