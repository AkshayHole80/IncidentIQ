package com.incidentIQ.incident_service.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequestDto {

    private Long userId;

    private String title;

    private String message;
}