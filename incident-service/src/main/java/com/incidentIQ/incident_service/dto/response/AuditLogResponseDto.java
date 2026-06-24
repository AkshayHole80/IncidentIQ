package com.incidentIQ.incident_service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogResponseDto {

    private String userName;
    private String action;
    private String details;
    private LocalDateTime createdAt;
}