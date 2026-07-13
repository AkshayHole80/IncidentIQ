package com.incidentIQ.incident_service.service.impl;

import com.incidentIQ.incident_service.dto.response.AuditLogResponseDto;
import com.incidentIQ.incident_service.entity.AuditLog;
import com.incidentIQ.incident_service.enums.AuditAction;
import com.incidentIQ.incident_service.event.AuditLogEvent;
import org.springframework.kafka.core.KafkaTemplate;
import com.incidentIQ.incident_service.repository.AuditLogRepository;
import com.incidentIQ.incident_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl
        implements AuditLogService {

    private final AuditLogRepository repository;
    private final ModelMapper modelMapper;
    private final KafkaTemplate<String, Object> genericKafkaTemplate;

    @Override
    public void log(
            Long incidentId,
            Long userId,
            String userName,
            AuditAction action,
            String details) {

        AuditLogEvent event = AuditLogEvent.builder()
                .incidentId(incidentId)
                .userId(userId)
                .userName(userName)
                .action(action.name())
                .details(details)
                .build();

        try {
            genericKafkaTemplate.send("audit-log-topic", event);
            log.info("Published AuditLogEvent to Kafka for incident id={}", incidentId);
        } catch (Exception e) {
            log.error("Failed to publish AuditLogEvent to Kafka, falling back to direct db save", e);
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
    }

    @Override
    public List<AuditLogResponseDto> getAuditLogs(
            Long incidentId) {

        return repository
                .findByIncidentIdOrderByCreatedAtAsc(
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