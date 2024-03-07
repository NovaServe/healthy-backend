package healthy.lifestyle.backend.notification.kafka;

import healthy.lifestyle.backend.notification.firebase.FirebaseMessageDto;
import healthy.lifestyle.backend.notification.firebase.FirebaseMessagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    @Autowired
    FirebaseMessagingService firebaseMessagingService;

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(
            topics = {
                "${spring.kafka.topic.activity.name}",
                "${spring.kafka.topic.billing.name}",
                "${spring.kafka.topic.system.name}"
            },
            groupId = "${spring.kafka.consumer.group-id}")
    public void consume(FirebaseMessageDto firebaseMessageDto) {

        logger.info(
                "Kafka consumer has received a message with title: {}, body: {}",
                firebaseMessageDto.getTitle(),
                firebaseMessageDto.getBody());
        firebaseMessagingService.sendNotificationByToken(firebaseMessageDto);
    }
}
