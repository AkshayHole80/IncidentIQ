package com.incidentIQ.incident_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IncidentStatsResponseDto {

    private long totalIncidents;

    private long openIncidents;

    private long inProgressIncidents;

    private long resolvedIncidents;

    private long closedIncidents;

    private long criticalIncidents;
}