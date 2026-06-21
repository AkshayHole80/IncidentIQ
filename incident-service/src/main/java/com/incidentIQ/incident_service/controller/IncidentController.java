package com.incidentIQ.incident_service.controller;
import com.incidentIQ.incident_service.dto.request.CreateIncidentRequestDto;
import com.incidentIQ.incident_service.dto.request.UpdateIncidentRequestDto;
import com.incidentIQ.incident_service.dto.response.IncidentResponseDto;
import com.incidentIQ.incident_service.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponseDto createIncident(
            @Valid @RequestBody CreateIncidentRequestDto request) {

        return incidentService.createIncident(request);
    }

    @GetMapping("/{id}")
    public IncidentResponseDto getIncidentById(
            @PathVariable Long id) {

        return incidentService.getIncidentById(id);
    }

    @GetMapping
    public List<IncidentResponseDto> getAllIncidents() {

        return incidentService.getAllIncidents();
    }

    @PutMapping("/{id}")
    public IncidentResponseDto updateIncident(
            @PathVariable Long id,
            @RequestBody UpdateIncidentRequestDto request) {

        return incidentService.updateIncident(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIncident(
            @PathVariable Long id) {

        incidentService.deleteIncident(id);
    }
}