package com.incidentIQ.notification_service.event;

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
}