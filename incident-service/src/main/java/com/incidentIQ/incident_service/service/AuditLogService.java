package com.incidentIQ.incident_service.service;

import com.incidentIQ.incident_service.dto.response.AuditLogResponseDto;
import com.incidentIQ.incident_service.enums.AuditAction;

import java.util.List;

public interface AuditLogService {

    void log(
            Long incidentId,
            Long userId,
            String userName,
            AuditAction action,
            String details
    );
    List<AuditLogResponseDto> getAuditLogs(
            Long incidentId
    );
}