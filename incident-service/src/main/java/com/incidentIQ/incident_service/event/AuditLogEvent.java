package com.incidentIQ.incident_service.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogEvent {
    private Long incidentId;
    private Long userId;
    private String userName;
    private String action;
    private String details;
}
