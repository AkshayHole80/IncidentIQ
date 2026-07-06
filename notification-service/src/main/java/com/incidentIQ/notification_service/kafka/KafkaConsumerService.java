package com.incidentIQ.notification_service.kafka;

import com.incidentIQ.notification_service.dto.request.CreateNotificationRequestDto;
import com.incidentIQ.notification_service.event.IncidentNotificationEvent;
import com.incidentIQ.notification_service.enums.NotificationType;
import com.incidentIQ.notification_service.service.EmailService;
import com.incidentIQ.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final NotificationService notificationService;
    private final EmailService emailService;

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

        if (event.getNotificationType() == NotificationType.ASSIGNED) {

            emailService.sendAssignmentEmail(event);

        }

        if (event.getNotificationType() == NotificationType.CRITICAL) {

            emailService.sendCriticalIncidentEmail(event);

        }

        System.out.println(
                "Notification processed : "
                        + event.getNotificationType()
        );
    }
}