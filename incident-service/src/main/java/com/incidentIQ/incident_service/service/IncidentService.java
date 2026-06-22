package com.incidentIQ.incident_service.service;

import com.incidentIQ.incident_service.dto.request.AssignIncidentRequestDto;
import com.incidentIQ.incident_service.dto.request.CreateIncidentRequestDto;
import com.incidentIQ.incident_service.dto.request.UpdateIncidentRequestDto;
import com.incidentIQ.incident_service.dto.response.IncidentResponseDto;
import com.incidentIQ.incident_service.dto.response.IncidentStatsResponseDto;
import com.incidentIQ.incident_service.enums.Category;
import com.incidentIQ.incident_service.enums.IncidentStatus;
import com.incidentIQ.incident_service.enums.Priority;

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
    IncidentResponseDto assignIncident(
            Long id,
            AssignIncidentRequestDto request
    );

    IncidentResponseDto updateStatus(
            Long id,
            IncidentStatus status
    );

    List<IncidentResponseDto> getAssignedIncidents();

    List<IncidentResponseDto> getIncidentsByStatus(
            IncidentStatus status
    );

    List<IncidentResponseDto> getIncidentsByPriority(
            Priority priority
    );
    List<IncidentResponseDto> getIncidentsByCategory(
            Category category
    );

    IncidentStatsResponseDto getIncidentStats();
    List<IncidentResponseDto> searchIncidents(
            String keyword
    );
}