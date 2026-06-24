package com.incidentIQ.notification_service.controller;

import com.incidentIQ.notification_service.dto.request.CreateNotificationRequestDto;
import com.incidentIQ.notification_service.dto.response.NotificationResponseDto;
import com.incidentIQ.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponseDto> createNotification(
            @RequestBody CreateNotificationRequestDto request) {
        return new ResponseEntity<>(notificationService.createNotification(request), HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDto>> getNotificationsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUser(userId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable Long userId) {
        return ResponseEntity.ok(Map.of(
                "count",
                notificationService.getUnreadCount(userId)
        ));
    }
}