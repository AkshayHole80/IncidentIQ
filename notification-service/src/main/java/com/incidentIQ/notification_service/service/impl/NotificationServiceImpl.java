package com.incidentIQ.notification_service.service.impl;

import com.incidentIQ.notification_service.dto.request.CreateNotificationRequestDto;
import com.incidentIQ.notification_service.dto.response.NotificationResponseDto;
import com.incidentIQ.notification_service.entity.Notification;
import com.incidentIQ.notification_service.repository.NotificationRepository;
import com.incidentIQ.notification_service.service.NotificationService;
import com.incidentIQ.notification_service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate;


    @Override
    public NotificationResponseDto createNotification(
            CreateNotificationRequestDto request) {

        Notification notification =
                Notification.builder()
                        .userId(request.getUserId())
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();

        Notification saved =
                notificationRepository.save(notification);
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + saved.getUserId(),
                saved
        );

        return modelMapper.map(
                saved,
                NotificationResponseDto.class
        );
    }

    @Override
    public List<NotificationResponseDto>
    getNotificationsByUser(Long userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notification ->
                        modelMapper.map(
                                notification,
                                NotificationResponseDto.class))
                .toList();
    }

    @Override
    public void markAsRead(Long notificationId) {

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found with id: " + notificationId));

        notification.setIsRead(true);

        notificationRepository.save(notification);
    }

    @Override
    public long getUnreadCount(Long userId) {

        return notificationRepository
                .countByUserIdAndIsReadFalse(userId);
    }
}
