package com.incidentIQ.incident_service.service;

import com.incidentIQ.incident_service.dto.request.CreateIncidentRequestDto;
import com.incidentIQ.incident_service.dto.request.UpdateIncidentRequestDto;
import com.incidentIQ.incident_service.dto.response.IncidentResponseDto;

import java.util.List;

public interface IncidentService {

    IncidentResponseDto createIncident(
            CreateIncidentRequestDto request);

    IncidentResponseDto getIncidentById(Long id);

    List<IncidentResponseDto> getAllIncidents();

    IncidentResponseDto updateIncident(
            Long id,
            UpdateIncidentRequestDto request);

    void deleteIncident(Long id);
}