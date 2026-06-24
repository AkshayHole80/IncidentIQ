package com.incidentIQ.incident_service.service.impl;

import com.incidentIQ.incident_service.dto.response.AuditLogResponseDto;
import com.incidentIQ.incident_service.entity.AuditLog;
import com.incidentIQ.incident_service.enums.AuditAction;
import com.incidentIQ.incident_service.repository.AuditLogRepository;
import com.incidentIQ.incident_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl
        implements AuditLogService {

    private final AuditLogRepository repository;
    private final ModelMapper modelMapper;

    @Override
    public void log(
            Long incidentId,
            Long userId,
            String userName,
            AuditAction action,
            String details) {

        repository.save(
                AuditLog.builder()
                        .incidentId(incidentId)
                        .userId(userId)
                        .userName(userName)
                        .action(action.name())
                        .details(details)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    @Override
    public List<AuditLogResponseDto> getAuditLogs(
            Long incidentId) {

        return repository
                .findByIncidentIdOrderByCreatedAtDesc(
                        incidentId
                )
                .stream()
                .map(log -> {

                    AuditLogResponseDto dto =
                            new AuditLogResponseDto();

                    dto.setUserName(
                            log.getUserName());

                    dto.setAction(
                            log.getAction());

                    dto.setDetails(
                            log.getDetails());

                    dto.setCreatedAt(
                            log.getCreatedAt());

                    return dto;
                })
                .toList();
    }



}