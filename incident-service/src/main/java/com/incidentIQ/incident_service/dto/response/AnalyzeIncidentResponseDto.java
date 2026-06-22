package com.incidentIQ.incident_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalyzeIncidentResponseDto {

    private String priority;
    private String category;
}