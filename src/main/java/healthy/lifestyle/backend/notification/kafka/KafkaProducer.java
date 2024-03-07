package healthy.lifestyle.backend.notification.kafka;

import healthy.lifestyle.backend.notification.firebase.FirebaseMessageDto;
import healthy.lifestyle.backend.notification.scheduler.TaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

    @Autowired
    KafkaTemplate<String, FirebaseMessageDto> kafkaTemplate;

    @Autowired
    KafkaConfig kafkaConfig;

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    public void sendMessage(TaskDto taskDto) {
        FirebaseMessageDto firebaseMessageDto = FirebaseMessageDto.buildFromTaskDto(taskDto);

        String topicName = null;
        switch (taskDto.getActivityType()) {
            case WORKOUT, NUTRITION, MENTAL_ACTIVITY -> topicName = kafkaConfig.getActivityTopic();
        }

        Message<FirebaseMessageDto> message = MessageBuilder.withPayload(firebaseMessageDto)
                .setHeader(KafkaHeaders.TOPIC, topicName)
                .setHeader(KafkaHeaders.GROUP_ID, kafkaConfig.getGroupId())
                .build();
        logger.info("Kafka producer is ready to send a message");
        kafkaTemplate.send(message);
    }
}
