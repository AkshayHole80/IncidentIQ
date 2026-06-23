package com.incidentIQ.incident_service.service.impl;

import com.incidentIQ.incident_service.client.AiServiceClient;
import com.incidentIQ.incident_service.client.NotificationServiceClient;
import com.incidentIQ.incident_service.client.UserServiceClient;
import com.incidentIQ.incident_service.dto.request.*;
import com.incidentIQ.incident_service.dto.response.AnalyzeIncidentResponseDto;
import com.incidentIQ.incident_service.dto.response.IncidentResponseDto;
import com.incidentIQ.incident_service.dto.response.IncidentStatsResponseDto;
import com.incidentIQ.incident_service.dto.response.UserResponseDto;
import com.incidentIQ.incident_service.entity.Incident;
import com.incidentIQ.incident_service.enums.Category;
import com.incidentIQ.incident_service.enums.IncidentStatus;
import com.incidentIQ.incident_service.enums.Priority;
import com.incidentIQ.incident_service.exception.ForbiddenException;
import com.incidentIQ.incident_service.exception.IncidentNotFoundException;
import com.incidentIQ.incident_service.exception.UnauthorizedActionException;
import com.incidentIQ.incident_service.repository.IncidentRepository;
import com.incidentIQ.incident_service.security.SecurityUtils;
import com.incidentIQ.incident_service.service.IncidentService;
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
    private final NotificationServiceClient notificationServiceClient;

    @Override
    public IncidentResponseDto createIncident(
            CreateIncidentRequestDto request) {

        String email =
                SecurityUtils.getCurrentUserEmail();

        log.info("Creating incident for user: {}", email);

        UserResponseDto user =
                userServiceClient.getUserByEmail(email);

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

        log.info(
                "Incident created successfully with id={}",
                saved.getId()
        );

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

        incidentRepository.delete(incident);

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
        notificationServiceClient.createNotification(
                CreateNotificationRequestDto.builder()
                        .userId(request.getAssignedTo())
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

        return IncidentStatsResponseDto.builder()

                .totalIncidents(
                        incidentRepository.count()
                )

                .openIncidents(
                        incidentRepository.countByStatus(
                                IncidentStatus.OPEN
                        )
                )

                .inProgressIncidents(
                        incidentRepository.countByStatus(
                                IncidentStatus.IN_PROGRESS
                        )
                )

                .resolvedIncidents(
                        incidentRepository.countByStatus(
                                IncidentStatus.RESOLVED
                        )
                )

                .closedIncidents(
                        incidentRepository.countByStatus(
                                IncidentStatus.CLOSED
                        )
                )

                .criticalIncidents(
                        incidentRepository.countByPriority(
                                Priority.CRITICAL
                        )
                )

                .build();
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

        return userServiceClient
                .getUserByEmail(email);
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
        List<UserResponseDto> admins =
                userServiceClient.getAdmins();

        for (UserResponseDto admin : admins) {

            notificationServiceClient.createNotification(
                    CreateNotificationRequestDto.builder()
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

        notificationServiceClient.createNotification(
                CreateNotificationRequestDto.builder()
                        .userId(
                                incident.getCreatedBy()
                        )
                        .title("Incident Closed")
                        .message(
                                "Your incident #"
                                        + saved.getId()
                                        + " has been closed"
                        )
                        .build()
        );

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