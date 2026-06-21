package com.incidentIQ.incident_service.repository;

import com.incidentIQ.incident_service.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository
        extends JpaRepository<Incident, Long> {
}
