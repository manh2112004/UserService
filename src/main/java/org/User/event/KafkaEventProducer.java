package org.User.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaEventProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEvent(String topic, KafkaEvent event) {
        try {
            log.info("Sending event to topic {}: {}", topic, event);
            kafkaTemplate.send(topic, event.getEventId(), event);
        } catch (Exception e) {
            log.error("Failed to send event to Kafka: ", e);
        }
    }
}
