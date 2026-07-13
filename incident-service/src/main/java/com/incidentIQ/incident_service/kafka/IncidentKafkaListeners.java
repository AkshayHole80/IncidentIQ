package com.incidentIQ.incident_service.kafka;

import com.incidentIQ.incident_service.client.UserServiceClient;
import com.incidentIQ.incident_service.dto.response.UserResponseDto;
import com.incidentIQ.incident_service.entity.AuditLog;
import com.incidentIQ.incident_service.entity.Incident;
import com.incidentIQ.incident_service.enums.Category;
import com.incidentIQ.incident_service.enums.NotificationType;
import com.incidentIQ.incident_service.enums.Priority;
import com.incidentIQ.incident_service.event.AiClassificationResultEvent;
import com.incidentIQ.incident_service.event.AuditLogEvent;
import com.incidentIQ.incident_service.event.IncidentNotificationEvent;
import com.incidentIQ.incident_service.repository.AuditLogRepository;
import com.incidentIQ.incident_service.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentKafkaListeners {

    private final IncidentRepository incidentRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserServiceClient userServiceClient;
    private final KafkaProducerService kafkaProducerService;

    @KafkaListener(topics = "ai-classification-topic", groupId = "incident-ai-group")
    public void consumeAiClassification(AiClassificationResultEvent event) {
        log.info("Received AiClassificationResultEvent for incident id={}", event.getIncidentId());
        try {
            Incident incident = incidentRepository.findById(event.getIncidentId())
                    .orElseThrow(() -> new RuntimeException("Incident not found: " + event.getIncidentId()));

            Priority newPriority = Priority.valueOf(event.getPriority().toUpperCase());
            Category newCategory = Category.valueOf(event.getCategory().toUpperCase());

            incident.setPriority(newPriority);
            incident.setCategory(newCategory);
            incidentRepository.save(incident);
            log.info("Successfully updated incident id={} with AI priority={} and category={}",
                    event.getIncidentId(), newPriority, newCategory);

            // If the priority is classified as CRITICAL, notify admins
            if (newPriority == Priority.CRITICAL) {
                log.info("Asynchronous AI classification returned CRITICAL. Notifying admins.");
                try {
                    List<UserResponseDto> admins = userServiceClient.getAdmins();
                    if (admins != null) {
                        for (UserResponseDto admin : admins) {
                            kafkaProducerService.sendNotification(
                                    IncidentNotificationEvent.builder()
                                            .userId(admin.getId())
                                            .recipientEmail(admin.getEmail())
                                            .recipientName(admin.getFirstName())
                                            .incidentId(incident.getId())
                                            .priority("CRITICAL")
                                            .assignedBy("AI-SERVICE")
                                            .notificationType(NotificationType.CRITICAL)
                                            .title("🚨 Critical Incident Alert (AI)")
                                            .message("An incident with ID " + incident.getId() + " has been classified as CRITICAL by AI. Title: " + incident.getTitle())
                                            .build()
                            );
                        }
                    }
                } catch (Exception ex) {
                    log.error("Failed to notify admins about AI critical incident", ex);
                }
            }
        } catch (Exception e) {
            log.error("Error processing AI classification event", e);
        }
    }

    @KafkaListener(topics = "audit-log-topic", groupId = "incident-audit-group")
    public void consumeAuditLog(AuditLogEvent event) {
        log.info("Received AuditLogEvent for incident id={} asynchronously", event.getIncidentId());
        try {
            AuditLog auditLog = AuditLog.builder()
                    .incidentId(event.getIncidentId())
                    .userId(event.getUserId())
                    .userName(event.getUserName())
                    .action(event.getAction())
                    .details(event.getDetails())
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(auditLog);
            log.info("Successfully persisted audit log asynchronously for incident id={}", event.getIncidentId());
        } catch (Exception e) {
            log.error("Error persisting audit log asynchronously", e);
        }
    }
}
