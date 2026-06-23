package com.incidentIQ.notification_service.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {

    private Long id;

    private String title;

    private String message;

    private Boolean isRead;

    private LocalDateTime createdAt;
}