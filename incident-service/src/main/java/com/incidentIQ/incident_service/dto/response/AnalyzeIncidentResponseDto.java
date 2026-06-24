package com.incidentIQ.incident_service.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@ToString
public class AnalyzeIncidentResponseDto {

    private String priority;
    private String category;
}