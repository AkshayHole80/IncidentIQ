package com.incidentIQ.incident_service.service.impl;

import com.incidentIQ.incident_service.client.AiServiceClient;
import com.incidentIQ.incident_service.client.UserServiceClient;
import com.incidentIQ.incident_service.dto.request.*;
import com.incidentIQ.incident_service.dto.response.AnalyzeIncidentResponseDto;
import com.incidentIQ.incident_service.dto.response.IncidentResponseDto;
import com.incidentIQ.incident_service.dto.response.IncidentStatsResponseDto;
import com.incidentIQ.incident_service.dto.response.UserResponseDto;
import com.incidentIQ.incident_service.entity.Incident;
import com.incidentIQ.incident_service.enums.*;
import com.incidentIQ.incident_service.event.IncidentNotificationEvent;
import com.incidentIQ.incident_service.exception.BadRequestException;
import com.incidentIQ.incident_service.exception.ForbiddenException;
import com.incidentIQ.incident_service.exception.IncidentNotFoundException;
import com.incidentIQ.incident_service.exception.UnauthorizedActionException;
import feign.FeignException;
import com.incidentIQ.incident_service.kafka.KafkaProducerService;
import com.incidentIQ.incident_service.entity.Attachment;
import com.incidentIQ.incident_service.repository.AttachmentRepository;
import com.incidentIQ.incident_service.repository.IncidentRepository;
import com.incidentIQ.incident_service.security.SecurityUtils;
import com.incidentIQ.incident_service.service.AuditLogService;
import com.incidentIQ.incident_service.service.IncidentService;
import com.incidentIQ.incident_service.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final ModelMapper modelMapper;
    private final UserServiceClient userServiceClient;
    private final AiServiceClient aiServiceClient;
    private final KafkaProducerService kafkaProducerService;
    private final AuditLogService auditLogService;
    private final AttachmentRepository attachmentRepository;
    private final S3Service s3Service;

    @Override
    public IncidentResponseDto createIncident(
            CreateIncidentRequestDto request) {

        String email =
                SecurityUtils.getCurrentUserEmail();

        log.info("Creating incident for user: {}", email);

        UserResponseDto user;
        try {
            user = userServiceClient.getUserByEmail(email);
            if (user == null) {
                log.error("User service returned null profile for email: {}", email);
                throw new BadRequestException("User profile not found for email: " + email);
            }
        } catch (FeignException ex) {
            log.error("Failed to retrieve user profile for email: {} from user-service: {}", email, ex.getMessage());
            throw new BadRequestException("Failed to retrieve user profile from user-service: " + ex.getMessage());
        }

        Priority priority = Priority.MEDIUM;
        Category category = Category.APPLICATION;

        try {

            log.info(
                    "Calling AI service for incident classification");

            AnalyzeIncidentRequestDto aiRequest =
                    new AnalyzeIncidentRequestDto();

            aiRequest.setTitle(request.getTitle());
            aiRequest.setDescription(request.getDescription());

            AnalyzeIncidentResponseDto aiResponse =
                    aiServiceClient.analyzeIncident(aiRequest);

            priority =
                    Priority.valueOf(
                            aiResponse.getPriority()
                    );

            category =
                    Category.valueOf(
                            aiResponse.getCategory()
                    );

            log.info(
                    "AI classified incident. Priority={}, Category={}",
                    priority,
                    category
            );

        } catch (Exception e) {

            log.error(
                    "Failed to classify incident using AI service. Using default values.",
                    e
            );
        }

        Incident incident = Incident.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(priority)
                .category(category)
                .status(IncidentStatus.OPEN)
                .createdBy(user.getId())
                .createdAt(LocalDateTime.now())
                .build();

        log.info(
                "Saving incident with title={}",
                request.getTitle()
        );

        Incident saved =
                incidentRepository.save(incident);
        auditLogService.log(
                saved.getId(),
                user.getId(),
                email,
                AuditAction.CREATED,
                "Incident created"
        );

        log.info(
                "Incident created successfully with id={}",
                saved.getId()
        );

        if (saved.getPriority() == Priority.CRITICAL) {
            log.info("Critical incident detected, notifying admins.");
            try {
                List<UserResponseDto> admins = userServiceClient.getAdmins();
                if (admins != null) {
                    for (UserResponseDto admin : admins) {
                        kafkaProducerService.sendNotification(
                                IncidentNotificationEvent.builder()
                                        .userId(admin.getId())
                                        .recipientEmail(admin.getEmail())
                                        .recipientName(admin.getFirstName())
                                        .incidentId(saved.getId())
                                        .priority("CRITICAL")
                                        .assignedBy(user.getFirstName())
                                        .notificationType(NotificationType.CRITICAL)
                                        .title("🚨 Critical Incident Alert")
                                        .message("Critical incident #" + saved.getId() + " has been raised: " + saved.getTitle())
                                        .build()
                        );
                    }
                }
            } catch (Exception ex) {
                log.error("Failed to notify admins about critical incident: {}", ex.getMessage());
            }
        }

        return modelMapper.map(
                saved,
                IncidentResponseDto.class
        );
    }

    @Override
    public IncidentResponseDto getIncidentById(Long id) {

        log.info("Fetching incident with id={}", id);

        Incident incident = incidentRepository
                .findById(id)
                .orElseThrow(() ->
                        new IncidentNotFoundException(
                                "Incident not found with id: " + id));
        validateIncidentAccess(incident);
        return modelMapper.map(
                incident,
                IncidentResponseDto.class
        );
    }

    @Override
    public List<IncidentResponseDto> getAllIncidents() {

        UserResponseDto user =
                getCurrentUser();

        log.info(
                "Fetching incidents for user id={}",
                user.getId()
        );

        return incidentRepository
                .findByCreatedBy(user.getId())
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

        log.info("Updating incident with id={}", id);

        Incident incident = incidentRepository
                .findById(id)
                .orElseThrow(() ->
                        new IncidentNotFoundException(
                                "Incident not found with id: " + id));

        validateIncidentOwnership(incident);

        if (incident.getStatus() != IncidentStatus.OPEN) {

            throw new UnauthorizedActionException(
                    "Only OPEN incidents can be edited"
            );
        }

        incident.setTitle(request.getTitle());
        incident.setDescription(request.getDescription());

        Incident updatedIncident =
                incidentRepository.save(incident);
        UserResponseDto currentUser = getCurrentUser();

        auditLogService.log(
                updatedIncident.getId(),
                currentUser.getId(),
                currentUser.getEmail(),
                AuditAction.UPDATED,
                "Incident updated"
        );

        log.info(
                "Incident updated successfully. Id={}",
                id
        );

        return modelMapper.map(
                updatedIncident,
                IncidentResponseDto.class
        );
    }

    @Override
    public void deleteIncident(Long id) {

        log.info("Deleting incident with id={}", id);

        Incident incident = incidentRepository
                .findById(id)
                .orElseThrow(() ->
                        new IncidentNotFoundException(
                                "Incident not found with id: " + id));

        validateIncidentOwnership(incident);

        if (incident.getStatus() != IncidentStatus.OPEN) {

            throw new UnauthorizedActionException(
                    "Only OPEN incidents can be deleted"
            );
        }

        // Delete associated attachments from S3 and database first
        List<Attachment> attachments = attachmentRepository.findByIncidentId(id);
        for (Attachment attachment : attachments) {
            try {
                log.info("Deleting S3 file with key={} for incident={}", attachment.getS3Key(), id);
                s3Service.deleteFile(attachment.getS3Key());
            } catch (Exception e) {
                log.error("Failed to delete attachment key={} from S3", attachment.getS3Key(), e);
            }
        }
        if (!attachments.isEmpty()) {
            attachmentRepository.deleteAll(attachments);
            log.info("Deleted {} database attachment records for incident={}", attachments.size(), id);
        }

        incidentRepository.delete(incident);
        UserResponseDto currentUser = getCurrentUser();

        auditLogService.log(
                incident.getId(),
                currentUser.getId(),
                currentUser.getEmail(),
                AuditAction.DELETED,
                "Incident deleted"
        );

        log.info(
                "Incident deleted successfully. Id={}",
                id
        );
    }


    private void validateIncidentOwnership(
            Incident incident) {

        UserResponseDto currentUser =
                getCurrentUser();

        if (!incident.getCreatedBy()
                .equals(currentUser.getId())) {

            throw new ForbiddenException(
                    "You are not authorized to access this incident"
            );
        }
    }

    @Override
    public IncidentResponseDto assignIncident(Long id, AssignIncidentRequestDto request) {
        UserResponseDto currentUser = getCurrentUser();

        if (!"ADMIN".equals(
                currentUser.getRole())) {

            throw new UnauthorizedActionException(
                    "Only ADMIN can assign incidents"
            );
        }

        Incident incident =
                incidentRepository.findById(id)
                        .orElseThrow(() ->
                                new IncidentNotFoundException(
                                        "Incident not found with id: " + id));

        incident.setAssignedTo(
                request.getAssignedTo());

        incident.setStatus(
                IncidentStatus.IN_PROGRESS);

        Incident saved =
                incidentRepository.save(
                        incident);
        UserResponseDto assignedEngineer =
                userServiceClient.getUserById(
                        request.getAssignedTo()
                );
        auditLogService.log(
                incident.getId(),
                currentUser.getId(),
                 currentUser.getEmail(),
                AuditAction.ASSIGNED,
                "Assigned to user "
                        + request.getAssignedTo()
        );
        kafkaProducerService.sendNotification(

                IncidentNotificationEvent.builder()

                        .userId(
                                assignedEngineer.getId()
                        )

                        .recipientEmail(
                                assignedEngineer.getEmail()
                        )

                        .recipientName(
                                assignedEngineer.getFirstName()
                        )

                        .incidentId(
                                saved.getId()
                        )

                        .priority(
                                saved.getPriority().name()
                        )

                        .assignedBy(
                                currentUser.getFirstName()
                        )

                        .notificationType(
                                NotificationType.ASSIGNED
                        )

                        .title("Incident Assigned")

                        .message(
                                "You have been assigned incident #"
                                        + saved.getId()
                        )

                        .build()

        );

        return modelMapper.map(
                saved,
                IncidentResponseDto.class);
    }

    @Override
    public IncidentResponseDto updateStatus(
            Long id,
            IncidentStatus status) {

        Incident incident =
                incidentRepository.findById(id)
                        .orElseThrow(() ->
                                new IncidentNotFoundException(
                                        "Incident not found with id: " + id));

        validateAssignedEngineer(incident);

        incident.setStatus(status);

        Incident saved =
                incidentRepository.save(incident);

        log.info(
                "Incident {} status updated to {}",
                id,
                status
        );

        return modelMapper.map(
                saved,
                IncidentResponseDto.class);
    }

    @Override
    public List<IncidentResponseDto> getAssignedIncidents() {

        UserResponseDto user =
                getCurrentUser();

        return incidentRepository
                .findByAssignedTo(user.getId())
                .stream()
                .map(incident ->
                        modelMapper.map(
                                incident,
                                IncidentResponseDto.class))
                .toList();
    }

    @Override
    public List<IncidentResponseDto> getIncidentsByStatus(
            IncidentStatus status) {

        return incidentRepository
                .findByStatus(status)
                .stream()
                .map(incident ->
                        modelMapper.map(
                                incident,
                                IncidentResponseDto.class))
                .toList();
    }

    @Override
    public List<IncidentResponseDto> getIncidentsByPriority(
            Priority priority) {

        return incidentRepository
                .findByPriority(priority)
                .stream()
                .map(incident ->
                        modelMapper.map(
                                incident,
                                IncidentResponseDto.class))
                .toList();
    }

    @Override
    public List<IncidentResponseDto> getIncidentsByCategory(
            Category category) {

        return incidentRepository
                .findByCategory(category)
                .stream()
                .map(incident ->
                        modelMapper.map(
                                incident,
                                IncidentResponseDto.class))
                .toList();
    }
    @Override
    public IncidentStatsResponseDto getIncidentStats() {
        UserResponseDto currentUser = getCurrentUser();
        String role = currentUser.getRole();
        Long userId = currentUser.getId();

        if ("ADMIN".equals(role)) {
            return IncidentStatsResponseDto.builder()
                    .totalIncidents(incidentRepository.count())
                    .openIncidents(incidentRepository.countByStatus(IncidentStatus.OPEN))
                    .inProgressIncidents(incidentRepository.countByStatus(IncidentStatus.IN_PROGRESS))
                    .resolvedIncidents(incidentRepository.countByStatus(IncidentStatus.RESOLVED))
                    .closedIncidents(incidentRepository.countByStatus(IncidentStatus.CLOSED))
                    .criticalIncidents(incidentRepository.countByPriority(Priority.CRITICAL))
                    .build();
        } else if ("SUPPORT_ENGINEER".equals(role)) {
            return IncidentStatsResponseDto.builder()
                    .totalIncidents(incidentRepository.countByAssignedTo(userId))
                    .openIncidents(incidentRepository.countByAssignedToAndStatus(userId, IncidentStatus.OPEN))
                    .inProgressIncidents(incidentRepository.countByAssignedToAndStatus(userId, IncidentStatus.IN_PROGRESS))
                    .resolvedIncidents(incidentRepository.countByAssignedToAndStatus(userId, IncidentStatus.RESOLVED))
                    .closedIncidents(incidentRepository.countByAssignedToAndStatus(userId, IncidentStatus.CLOSED))
                    .criticalIncidents(incidentRepository.countByAssignedToAndPriority(userId, Priority.CRITICAL))
                    .build();
        } else { // USER / default
            return IncidentStatsResponseDto.builder()
                    .totalIncidents(incidentRepository.countByCreatedBy(userId))
                    .openIncidents(incidentRepository.countByCreatedByAndStatus(userId, IncidentStatus.OPEN))
                    .inProgressIncidents(incidentRepository.countByCreatedByAndStatus(userId, IncidentStatus.IN_PROGRESS))
                    .resolvedIncidents(incidentRepository.countByCreatedByAndStatus(userId, IncidentStatus.RESOLVED))
                    .closedIncidents(incidentRepository.countByCreatedByAndStatus(userId, IncidentStatus.CLOSED))
                    .criticalIncidents(incidentRepository.countByCreatedByAndPriority(userId, Priority.CRITICAL))
                    .build();
        }
    }

    @Override
    public List<IncidentResponseDto> searchIncidents(
            String keyword) {

        return incidentRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        keyword,
                        keyword
                )
                .stream()
                .map(incident ->
                        modelMapper.map(
                                incident,
                                IncidentResponseDto.class))
                .toList();
    }

    private UserResponseDto getCurrentUser() {

        String email =
                SecurityUtils.getCurrentUserEmail();

        try {
            UserResponseDto user = userServiceClient.getUserByEmail(email);
            if (user == null) {
                log.error("User service returned null profile for email: {}", email);
                throw new UnauthorizedActionException("User profile not found for email: " + email);
            }
            return user;
        } catch (FeignException ex) {
            log.error("Failed to retrieve user profile for email: {} from user-service: {}", email, ex.getMessage());
            throw new UnauthorizedActionException("Failed to retrieve user profile from user-service: " + ex.getMessage());
        }
    }

    private void validateAssignedEngineer(
            Incident incident) {

        UserResponseDto currentUser =
                getCurrentUser();

        if (incident.getAssignedTo() == null ||
                !incident.getAssignedTo()
                        .equals(currentUser.getId())) {

            throw new UnauthorizedActionException(
                    "Only assigned engineer can update incident status"
            );
        }
    }

    @Override
    public IncidentResponseDto resolveIncident(
            Long id,
            ResolveIncidentRequestDto request) {

        Incident incident =
                incidentRepository.findById(id)
                        .orElseThrow(() ->
                                new IncidentNotFoundException(
                                        "Incident not found with id: " + id));

        validateAssignedEngineer(incident);

        if (incident.getStatus()
                != IncidentStatus.IN_PROGRESS) {

            throw new IllegalStateException(
                    "Only IN_PROGRESS incidents can be resolved"
            );
        }

        incident.setResolutionNotes(
                request.getResolutionNotes());

        incident.setStatus(
                IncidentStatus.RESOLVED);

        Incident saved =
                incidentRepository.save(
                        incident);
        UserResponseDto currentUser =
                getCurrentUser();

        auditLogService.log(
                saved.getId(),
                currentUser.getId(),
                currentUser.getEmail(),
                AuditAction.RESOLVED,
                "Incident resolved"
        );
        List<UserResponseDto> admins;
        try {
            admins = userServiceClient.getAdmins();
            if (admins == null) {
                log.error("User service returned null admins list");
                admins = List.of();
            }
        } catch (FeignException ex) {
            log.error("Failed to retrieve admins list from user-service: {}", ex.getMessage());
            throw new UnauthorizedActionException("Failed to retrieve admin list from user-service: " + ex.getMessage());
        }


        for (UserResponseDto admin : admins) {

            kafkaProducerService.sendNotification(
                    IncidentNotificationEvent.builder()
                            .userId(admin.getId())
                            .title("Incident Resolved")
                            .message(
                                    "Incident #"
                                            + saved.getId()
                                            + " has been resolved"
                                    )
                            .build()
            );
        }

        try {
            UserResponseDto owner = userServiceClient.getUserById(saved.getCreatedBy());
            if (owner != null) {
                kafkaProducerService.sendNotification(
                        IncidentNotificationEvent.builder()
                                .userId(owner.getId())
                                .recipientEmail(owner.getEmail())
                                .recipientName(owner.getFirstName())
                                .incidentId(saved.getId())
                                .priority(saved.getPriority().name())
                                .assignedBy(currentUser.getFirstName())
                                .notificationType(NotificationType.RESOLVED)
                                .title("Incident Resolved")
                                .message("Your incident #" + saved.getId() + " has been resolved")
                                .build()
                );
            }
        } catch (Exception ex) {
            log.error("Failed to notify incident owner about resolution: {}", ex.getMessage());
        }

        return modelMapper.map(
                saved,
                IncidentResponseDto.class);
    }

    @Override
    public IncidentResponseDto closeIncident(
            Long id) {

        UserResponseDto currentUser =
                getCurrentUser();

        if (!"ADMIN".equals(
                currentUser.getRole())) {

            throw new UnauthorizedActionException(
                    "Only ADMIN can close incidents"
            );
        }

        Incident incident =
                incidentRepository.findById(id)
                        .orElseThrow(() ->
                                new IncidentNotFoundException(
                                        "Incident not found with id: " + id));

        if (incident.getStatus()
                != IncidentStatus.RESOLVED) {

            throw new IllegalStateException(
                    "Only RESOLVED incidents can be closed"
            );
        }

        incident.setStatus(
                IncidentStatus.CLOSED
        );

        Incident saved =
                incidentRepository.save(
                        incident
                );
        auditLogService.log(
                saved.getId(),
                currentUser.getId(),
                currentUser.getEmail(),
                AuditAction.CLOSED,
                "Incident closed"
        );

        try {
            UserResponseDto owner = userServiceClient.getUserById(saved.getCreatedBy());
            if (owner != null) {
                kafkaProducerService.sendNotification(
                        IncidentNotificationEvent.builder()
                                .userId(owner.getId())
                                .recipientEmail(owner.getEmail())
                                .recipientName(owner.getFirstName())
                                .incidentId(saved.getId())
                                .priority(saved.getPriority().name())
                                .assignedBy(currentUser.getFirstName())
                                .notificationType(NotificationType.CLOSED)
                                .title("Incident Closed")
                                .message(
                                        "Your incident #"
                                                + saved.getId()
                                                + " has been closed"
                                )
                                .build()
                );
            }
        } catch (Exception ex) {
            log.error("Failed to notify incident owner about closure: {}", ex.getMessage());
            kafkaProducerService.sendNotification(
                    IncidentNotificationEvent.builder()
                            .userId(saved.getCreatedBy())
                            .title("Incident Closed")
                            .message(
                                    "Your incident #"
                                            + saved.getId()
                                            + " has been closed"
                            )
                            .build()
            );
        }
        log.info(
                "Incident {} closed by admin {}",
                id,
                currentUser.getEmail()
        );

        return modelMapper.map(
                saved,
                IncidentResponseDto.class
        );
    }

    private void validateIncidentAccess(
            Incident incident) {

        UserResponseDto currentUser =
                getCurrentUser();

        if ("ADMIN".equals(
                currentUser.getRole())) {

            return;
        }

        boolean isCreator =
                incident.getCreatedBy()
                        .equals(currentUser.getId());

        boolean isAssigned =
                incident.getAssignedTo() != null
                        && incident.getAssignedTo()
                        .equals(currentUser.getId());

        if (!isCreator && !isAssigned) {

            throw new ForbiddenException(
                    "You are not authorized to access this incident"
            );
        }
    }

}