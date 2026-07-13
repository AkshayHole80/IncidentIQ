package com.incidentIQ.incident_service.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentCreatedEvent {
    private Long incidentId;
    private String title;
    private String description;
}
