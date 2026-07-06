package com.incidentIQ.incident_service.event;

import com.incidentIQ.incident_service.enums.NotificationType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentNotificationEvent {

    private Long userId;
    private String title;
    private String message;
    private String recipientEmail;
    private String recipientName;

    private Long incidentId;

    private String priority;

    private String assignedBy;

    private NotificationType notificationType;

}