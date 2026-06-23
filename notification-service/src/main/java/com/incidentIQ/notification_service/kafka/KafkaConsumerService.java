package com.incidentIQ.notification_service.kafka;

import com.incidentIQ.notification_service.dto.request.CreateNotificationRequestDto;
import com.incidentIQ.notification_service.event.IncidentNotificationEvent;
import com.incidentIQ.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
 import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "incident-notifications",
            groupId = "notification-group"
    )
    public void consume(
            IncidentNotificationEvent event) {

        notificationService.createNotification(
                CreateNotificationRequestDto.builder()
                        .userId(event.getUserId())
                        .title(event.getTitle())
                        .message(event.getMessage())
                        .build()
        );

        System.out.println(
                "Notification Saved -> "
                        + event.getTitle()
        );
    }
}