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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
@Tag(name = "Incident Controller", description = "Endpoints for reporting, assigning, updating, and tracking incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final AuditLogService auditLogService;

    @PostMapping
    @Operation(summary = "Create a new incident", description = "Creates a new system incident reported by a user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Incident successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<IncidentResponseDto> createIncident(
            @Valid @RequestBody CreateIncidentRequestDto request) {
        return new ResponseEntity<>(incidentService.createIncident(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get incident by ID", description = "Fetches an incident profile by its database ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incident found successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponseDto> getIncidentById(
            @PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getIncidentById(id));
    }

    @GetMapping
    @Operation(summary = "Get all incidents", description = "Retrieves all incidents globally in the system (Admins receive all, Support/Users see authorized ones)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incidents list retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<IncidentResponseDto>> getAllIncidents() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an incident", description = "Updates general attributes of an incident")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incident updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponseDto> updateIncident(
            @PathVariable Long id,
            @RequestBody UpdateIncidentRequestDto request) {
        return ResponseEntity.ok(incidentService.updateIncident(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an incident", description = "Removes an incident from the system")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Incident deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Forbidden action"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<Void> deleteIncident(
            @PathVariable Long id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Assign incident to engineer", description = "Assigns a reported incident to a support engineer (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incident assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Forbidden: only Admins can assign incidents"),
            @ApiResponse(responseCode = "404", description = "Incident or engineer not found")
    })
    public ResponseEntity<IncidentResponseDto> assignIncident(
            @PathVariable Long id,
            @RequestBody AssignIncidentRequestDto request) {
        return ResponseEntity.ok(incidentService.assignIncident(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update incident status", description = "Changes the lifecycle status of an incident")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incident status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request status value"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Forbidden action"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponseDto> updateStatus(
            @PathVariable Long id,
            @RequestParam IncidentStatus status) {
        return ResponseEntity.ok(incidentService.updateStatus(id, status));
    }

    @GetMapping("/assigned")
    @Operation(summary = "Get incidents assigned to current user", description = "Retrieves all incidents assigned to the logged-in engineer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assigned incidents list retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<IncidentResponseDto>> getAssignedIncidents() {
        return ResponseEntity.ok(incidentService.getAssignedIncidents());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get incidents by status", description = "Retrieves all incidents filtered by a specific status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of incidents returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<IncidentResponseDto>> getByStatus(
            @PathVariable IncidentStatus status) {
        return ResponseEntity.ok(incidentService.getIncidentsByStatus(status));
    }

    @GetMapping("/priority/{priority}")
    @Operation(summary = "Get incidents by priority", description = "Retrieves all incidents filtered by priority level")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of incidents returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<IncidentResponseDto>> getByPriority(
            @PathVariable Priority priority) {
        return ResponseEntity.ok(incidentService.getIncidentsByPriority(priority));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get incidents by category", description = "Retrieves all incidents filtered by category name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of incidents returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<IncidentResponseDto>> getByCategory(
            @PathVariable Category category) {
        return ResponseEntity.ok(incidentService.getIncidentsByCategory(category));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get role-aware dashboard stats", description = "Retrieves incident stats counts customized based on user role (Admin: global, Support: assigned, User: owned)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stats object returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<IncidentStatsResponseDto> getStats() {
        return ResponseEntity.ok(incidentService.getIncidentStats());
    }

    @GetMapping("/search")
    @Operation(summary = "Search incidents by keyword", description = "Searches for incidents whose title or description contains the keyword")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results list returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<IncidentResponseDto>> searchIncidents(
            @RequestParam String keyword) {
        return ResponseEntity.ok(incidentService.searchIncidents(keyword));
    }

    @PatchMapping("/{id}/resolve")
    @Operation(summary = "Resolve an incident", description = "Marks an incident as RESOLVED with resolution notes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incident successfully resolved"),
            @ApiResponse(responseCode = "400", description = "Invalid request resolution notes"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Forbidden action"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponseDto> resolveIncident(
            @PathVariable Long id,
            @RequestBody ResolveIncidentRequestDto request) {
        return ResponseEntity.ok(incidentService.resolveIncident(id, request));
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Close an incident", description = "Marks a resolved incident as CLOSED (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incident successfully closed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Forbidden: only Admins can close incidents"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponseDto> closeIncident(
            @PathVariable Long id) {
        return ResponseEntity.ok(incidentService.closeIncident(id));
    }

    @GetMapping("/{id}/audit-logs")
    @Operation(summary = "Get incident audit logs", description = "Retrieves historical audit trail/logs for a specific incident")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit logs list returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<List<AuditLogResponseDto>> getAuditLogs(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(auditLogService.getAuditLogs(id));
    }
}