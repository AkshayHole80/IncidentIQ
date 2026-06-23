package com.incidentIQ.incident_service.kafka;

import com.incidentIQ.incident_service.event.IncidentNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String,
                IncidentNotificationEvent> kafkaTemplate;

    public void sendNotification(
            IncidentNotificationEvent event) {

        kafkaTemplate.send(
                "incident-notifications",
                event
        );

        System.out.println(
                "Notification Event Sent: "
                        + event.getTitle()
        );
    }
}