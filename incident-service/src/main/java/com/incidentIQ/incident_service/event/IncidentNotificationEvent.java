package com.incidentIQ.incident_service.event;

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