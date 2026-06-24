package com.incidentIQ.incident_service.controller;

import com.incidentIQ.incident_service.dto.request.AssignIncidentRequestDto;
import com.incidentIQ.incident_service.dto.request.CreateIncidentRequestDto;
import com.incidentIQ.incident_service.dto.request.ResolveIncidentRequestDto;
import com.incidentIQ.incident_service.dto.request.UpdateIncidentRequestDto;
import com.incidentIQ.incident_service.dto.response.AuditLogResponseDto;
import com.incidentIQ.incident_service.dto.response.IncidentResponseDto;
import com.incidentIQ.incident_service.dto.response.IncidentStatsResponseDto;
import com.incidentIQ.incident_service.enums.Category;
import com.incidentIQ.incident_service.enums.IncidentStatus;
import com.incidentIQ.incident_service.enums.Priority;
import com.incidentIQ.incident_service.service.AuditLogService;
import com.incidentIQ.incident_service.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;
    private final AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<IncidentResponseDto> createIncident(
            @Valid @RequestBody CreateIncidentRequestDto request) {
        return new ResponseEntity<>(incidentService.createIncident(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponseDto> getIncidentById(
            @PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getIncidentById(id));
    }

    @GetMapping
    public ResponseEntity<List<IncidentResponseDto>> getAllIncidents() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncidentResponseDto> updateIncident(
            @PathVariable Long id,
            @RequestBody UpdateIncidentRequestDto request) {
        return ResponseEntity.ok(incidentService.updateIncident(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(
            @PathVariable Long id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<IncidentResponseDto> assignIncident(
            @PathVariable Long id,
            @RequestBody AssignIncidentRequestDto request) {
        return ResponseEntity.ok(incidentService.assignIncident(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<IncidentResponseDto> updateStatus(
            @PathVariable Long id,
            @RequestParam IncidentStatus status) {
        return ResponseEntity.ok(incidentService.updateStatus(id, status));
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<IncidentResponseDto>> getAssignedIncidents() {
        return ResponseEntity.ok(incidentService.getAssignedIncidents());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<IncidentResponseDto>> getByStatus(
            @PathVariable IncidentStatus status) {
        return ResponseEntity.ok(incidentService.getIncidentsByStatus(status));
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<IncidentResponseDto>> getByPriority(
            @PathVariable Priority priority) {
        return ResponseEntity.ok(incidentService.getIncidentsByPriority(priority));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<IncidentResponseDto>> getByCategory(
            @PathVariable Category category) {
        return ResponseEntity.ok(incidentService.getIncidentsByCategory(category));
    }

    @GetMapping("/stats")
    public ResponseEntity<IncidentStatsResponseDto> getStats() {
        return ResponseEntity.ok(incidentService.getIncidentStats());
    }

    @GetMapping("/search")
    public ResponseEntity<List<IncidentResponseDto>> searchIncidents(
            @RequestParam String keyword) {
        return ResponseEntity.ok(incidentService.searchIncidents(keyword));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<IncidentResponseDto> resolveIncident(
            @PathVariable Long id,
            @RequestBody ResolveIncidentRequestDto request) {
        return ResponseEntity.ok(incidentService.resolveIncident(id, request));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<IncidentResponseDto> closeIncident(
            @PathVariable Long id) {
        return ResponseEntity.ok(incidentService.closeIncident(id));
    }

    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<List<AuditLogResponseDto>> getAuditLogs(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(auditLogService.getAuditLogs(id));
    }
}