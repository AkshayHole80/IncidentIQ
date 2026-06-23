package com.incidentIQ.notification_service.service;

import com.incidentIQ.notification_service.dto.request.CreateNotificationRequestDto;
import com.incidentIQ.notification_service.dto.response.NotificationResponseDto;

import java.util.List;

public interface NotificationService {

    NotificationResponseDto createNotification(
            CreateNotificationRequestDto request);

    List<NotificationResponseDto>
    getNotificationsByUser(Long userId);

    void markAsRead(Long notificationId);

    long getUnreadCount(Long userId);
}