package com.incidentIQ.incident_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalyzeIncidentRequestDto {

    private String title;
    private String description;
}
