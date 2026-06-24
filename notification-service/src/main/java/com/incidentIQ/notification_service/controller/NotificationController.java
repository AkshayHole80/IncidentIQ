package com.incidentIQ.notification_service.controller;

import com.incidentIQ.notification_service.dto.request.CreateNotificationRequestDto;
import com.incidentIQ.notification_service.dto.response.NotificationResponseDto;
import com.incidentIQ.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "Endpoints for creating and retrieving system notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create system notification", description = "Creates and sends a system-wide or user-specific notification")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notification successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request input data")
    })
    public ResponseEntity<NotificationResponseDto> createNotification(
            @RequestBody CreateNotificationRequestDto request) {
        return new ResponseEntity<>(notificationService.createNotification(request), HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notifications for user", description = "Retrieves all notifications associated with a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications list returned successfully")
    })
    public ResponseEntity<List<NotificationResponseDto>> getNotificationsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUser(userId));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Updates a notification status to read")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification marked as read successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/unread-count")
    @Operation(summary = "Get unread notifications count", description = "Retrieves the count of unread notifications for a specific user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unread notifications count returned successfully")
    })
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable Long userId) {
        return ResponseEntity.ok(Map.of(
                "count",
                notificationService.getUnreadCount(userId)
        ));
    }
}