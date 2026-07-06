package com.incidentIQ.incident_service.kafka;

import com.incidentIQ.incident_service.event.IncidentNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private static final String TOPIC = "incident-notifications";

    private final KafkaTemplate<String, IncidentNotificationEvent> kafkaTemplate;

    public void publishNotification(IncidentNotificationEvent event) {

        log.info("Publishing notification event : {}", event);

        kafkaTemplate.send(TOPIC, event);

    }

}