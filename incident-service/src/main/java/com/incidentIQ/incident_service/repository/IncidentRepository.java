package com.incidentIQ.incident_service.repository;

import com.incidentIQ.incident_service.entity.Incident;
import com.incidentIQ.incident_service.enums.Category;
import com.incidentIQ.incident_service.enums.IncidentStatus;
import com.incidentIQ.incident_service.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository
        extends JpaRepository<Incident, Long> {
    List<Incident> findByCreatedBy(Long createdBy);
    List<Incident> findByAssignedTo(Long assignedTo);
    List<Incident> findByStatus(
            IncidentStatus status
    );

    List<Incident> findByPriority(
            Priority priority
    );

    List<Incident> findByCategory(
            Category category
    );

    long countByStatus(
            IncidentStatus status
    );

    long countByPriority(
            Priority priority
    );

    List<Incident> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title,
            String description
    );
}
