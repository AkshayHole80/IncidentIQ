package com.incidentIQ.incident_service.dto.response;
import com.incidentIQ.incident_service.enums.IncidentStatus;
import com.incidentIQ.incident_service.enums.Priority;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentResponseDto {

    private Long id;

    private String title;

    private String description;

    private Priority priority;

    private IncidentStatus status;

    private Long createdBy;

    private LocalDateTime createdAt;
}
