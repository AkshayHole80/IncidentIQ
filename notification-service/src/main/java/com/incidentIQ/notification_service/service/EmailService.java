package com.incidentIQ.notification_service.service;

import com.incidentIQ.notification_service.event.IncidentNotificationEvent;
import org.springframework.http.ResponseEntity;

public interface EmailService {

    ResponseEntity<String> sendTestEmail(String to);

    void sendAssignmentEmail(IncidentNotificationEvent event);

    void sendCriticalIncidentEmail(IncidentNotificationEvent event);

}