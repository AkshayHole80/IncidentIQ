package com.incidentIQ.incident_service.service.impl;

import com.incidentIQ.incident_service.client.AiServiceClient;
import com.incidentIQ.incident_service.client.UserServiceClient;
import com.incidentIQ.incident_service.dto.request.AnalyzeIncidentRequestDto;
import com.incidentIQ.incident_service.dto.request.CreateIncidentRequestDto;
import com.incidentIQ.incident_service.dto.request.UpdateIncidentRequestDto;
import com.incidentIQ.incident_service.dto.response.AnalyzeIncidentResponseDto;
import com.incidentIQ.incident_service.dto.response.IncidentResponseDto;
import com.incidentIQ.incident_service.dto.response.UserResponseDto;
import com.incidentIQ.incident_service.entity.Incident;
import com.incidentIQ.incident_service.enums.Category;
import com.incidentIQ.incident_service.enums.IncidentStatus;
import com.incidentIQ.incident_service.enums.Priority;
import com.incidentIQ.incident_service.exception.ForbiddenException;
import com.incidentIQ.incident_service.exception.IncidentNotFoundException;
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
        validateIncidentOwnership(incident);
        return modelMapper.map(
                incident,
                IncidentResponseDto.class
        );
    }

    @Override
    public List<IncidentResponseDto> getAllIncidents() {

        String email =
                SecurityUtils.getCurrentUserEmail();

        UserResponseDto user =
                userServiceClient.getUserByEmail(email);

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
        incident.setTitle(request.getTitle());
        incident.setDescription(request.getDescription());

        if (request.getPriority() != null) {
            incident.setPriority(request.getPriority());
        }

        if (request.getStatus() != null) {
            incident.setStatus(request.getStatus());
        }

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
        incidentRepository.delete(incident);

        log.info(
                "Incident deleted successfully. Id={}",
                id
        );
    }


    private void validateIncidentOwnership(
            Incident incident) {

        String email =
                SecurityUtils.getCurrentUserEmail();

        UserResponseDto currentUser =
                userServiceClient.getUserByEmail(email);

        if (!incident.getCreatedBy()
                .equals(currentUser.getId())) {

            throw new ForbiddenException(
                    "You are not authorized to access this incident"
            );
        }
    }
}