package com.incidentIQ.notification_service.controller;

import com.incidentIQ.notification_service.dto.request.CreateNotificationRequestDto;
import com.incidentIQ.notification_service.dto.response.NotificationResponseDto;
import com.incidentIQ.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public NotificationResponseDto createNotification(
            @RequestBody CreateNotificationRequestDto request) {

        return notificationService
                .createNotification(request);
    }

    @GetMapping("/user/{userId}")
    public List<NotificationResponseDto>
    getNotificationsByUser(
            @PathVariable Long userId) {

        return notificationService
                .getNotificationsByUser(userId);
    }

    @PatchMapping("/{id}/read")
    public void markAsRead(
            @PathVariable Long id) {

        notificationService.markAsRead(id);
    }

    @GetMapping("/user/{userId}/unread-count")
    public Map<String, Long> getUnreadCount(
            @PathVariable Long userId) {

        return Map.of(
                "count",
                notificationService
                        .getUnreadCount(userId)
        );
    }
}