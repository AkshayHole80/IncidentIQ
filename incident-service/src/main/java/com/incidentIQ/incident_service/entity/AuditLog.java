package com.incidentIQ.incident_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long incidentId;

    private Long userId;

    private String userName;

    private String action;

    private String details;

    private LocalDateTime createdAt;

}