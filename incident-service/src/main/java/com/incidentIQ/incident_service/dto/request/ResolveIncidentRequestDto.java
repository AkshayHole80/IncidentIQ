package com.incidentIQ.incident_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResolveIncidentRequestDto {

    @NotBlank
    private String resolutionNotes;
}
