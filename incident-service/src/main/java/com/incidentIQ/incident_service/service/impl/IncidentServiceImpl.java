package com.incidentIQ.incident_service.service.impl;

import com.incidentIQ.incident_service.dto.request.CreateIncidentRequestDto;
import com.incidentIQ.incident_service.dto.request.UpdateIncidentRequestDto;
import com.incidentIQ.incident_service.dto.response.IncidentResponseDto;
import com.incidentIQ.incident_service.entity.Incident;
import com.incidentIQ.incident_service.enums.IncidentStatus;
import com.incidentIQ.incident_service.enums.Priority;
import com.incidentIQ.incident_service.exception.IncidentNotFoundException;
import com.incidentIQ.incident_service.repository.IncidentRepository;
import com.incidentIQ.incident_service.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final ModelMapper modelMapper;

    @Override
    public IncidentResponseDto createIncident(
            CreateIncidentRequestDto request) {

        Incident incident = Incident.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(Priority.MEDIUM)
                .status(IncidentStatus.OPEN)
                .createdBy(1L) // Temporary
                .createdAt(LocalDateTime.now())
                .build();

        Incident savedIncident =
                incidentRepository.save(incident);

        return modelMapper.map(
                savedIncident,
                IncidentResponseDto.class
        );
    }

    @Override
    public IncidentResponseDto getIncidentById(Long id) {

        Incident incident = incidentRepository
                .findById(id)
                .orElseThrow(() ->
                        new IncidentNotFoundException(
                                "Incident not found with id: " + id));

        return modelMapper.map(
                incident,
                IncidentResponseDto.class
        );
    }


    @Override
    public List<IncidentResponseDto> getAllIncidents() {

        return incidentRepository.findAll()
                .stream()
                .map(incident ->
                        modelMapper.map(
                                incident,
                                IncidentResponseDto.class))
                .toList();
    }

    @Override
    public IncidentResponseDto updateIncident(
            Long id,
            UpdateIncidentRequestDto request) {

        Incident incident = incidentRepository
                .findById(id)
                .orElseThrow(() ->
                        new IncidentNotFoundException(
                                "Incident not found with id: " + id));

        incident.setTitle(request.getTitle());
        incident.setDescription(request.getDescription());

        if (request.getPriority() != null) {
            incident.setPriority(request.getPriority());
        }

        if (request.getStatus() != null) {
            incident.setStatus(request.getStatus());
        }

        Incident updatedIncident =
                incidentRepository.save(incident);

        return modelMapper.map(
                updatedIncident,
                IncidentResponseDto.class
        );
    }

    @Override
    public void deleteIncident(Long id) {

        Incident incident = incidentRepository
                .findById(id)
                .orElseThrow(() ->
                        new IncidentNotFoundException(
                                "Incident not found with id: " + id));

        incidentRepository.delete(incident);
    }
}
