package healthy.lifestyle.backend.notification.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Service
public class KafkaConfig {
    @Value("${spring.kafka.topic.activity.name}")
    private String ActivityTopic;

    @Value("${spring.kafka.topic.billing.name}")
    private String billingTopic;

    @Value("${spring.kafka.topic.system.name}")
    private String systemTopic;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
}
