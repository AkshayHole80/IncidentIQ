package com.incidentIQ.incident_service.repository;

import com.incidentIQ.incident_service.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository
        extends JpaRepository<Incident, Long> {
    List<Incident> findByCreatedBy(Long createdBy);
}
