package com.incidentIQ.incident_service.controller;
import com.incidentIQ.incident_service.dto.request.AssignIncidentRequestDto;
import com.incidentIQ.incident_service.dto.request.CreateIncidentRequestDto;
import com.incidentIQ.incident_service.dto.request.UpdateIncidentRequestDto;
import com.incidentIQ.incident_service.dto.response.IncidentResponseDto;
import com.incidentIQ.incident_service.dto.response.IncidentStatsResponseDto;
import com.incidentIQ.incident_service.enums.Category;
import com.incidentIQ.incident_service.enums.IncidentStatus;
import com.incidentIQ.incident_service.enums.Priority;
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

    @PutMapping("/{id}/assign")
    public IncidentResponseDto assignIncident(
            @PathVariable Long id,
            @RequestBody AssignIncidentRequestDto request) {

        return incidentService.assignIncident(
                id,
                request
        );
    }

    @PatchMapping("/{id}/status")
    public IncidentResponseDto updateStatus(
            @PathVariable Long id,
            @RequestParam IncidentStatus status) {

        return incidentService
                .updateStatus(id, status);
    }

    @GetMapping("/assigned")
    public List<IncidentResponseDto> getAssignedIncidents() {

        return incidentService.getAssignedIncidents();
    }

    @GetMapping("/status/{status}")
    public List<IncidentResponseDto> getByStatus(
            @PathVariable IncidentStatus status) {

        return incidentService
                .getIncidentsByStatus(status);
    }

    @GetMapping("/priority/{priority}")
    public List<IncidentResponseDto> getByPriority(
            @PathVariable Priority priority) {

        return incidentService
                .getIncidentsByPriority(priority);
    }
    @GetMapping("/category/{category}")
    public List<IncidentResponseDto> getByCategory(
            @PathVariable Category category) {

        return incidentService
                .getIncidentsByCategory(category);
    }

    @GetMapping("/stats")
    public IncidentStatsResponseDto getStats() {

        return incidentService.getIncidentStats();
    }

    @GetMapping("/search")
    public List<IncidentResponseDto> searchIncidents(
            @RequestParam String keyword) {

        return incidentService.searchIncidents(
                keyword
        );
    }
}