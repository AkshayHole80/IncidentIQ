package com.incidentIQ.incident_service.repository;

import com.incidentIQ.incident_service.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByIncidentIdOrderByCreatedAtDesc(
            Long incidentId
    );

}