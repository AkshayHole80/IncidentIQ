package com.incidentIQ.incident_service.client;

import com.incidentIQ.incident_service.dto.request.CreateNotificationRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationServiceClient {

    @PostMapping("/api/v1/notifications")
    void createNotification(
            @RequestBody CreateNotificationRequestDto request
    );
}