package com.incidentIQ.incident_service.dto.request;

import com.incidentIQ.incident_service.enums.IncidentStatus;
import com.incidentIQ.incident_service.enums.Priority;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateIncidentRequestDto {

    private String title;

    private String description;

    private Priority priority;

    private IncidentStatus status;
}