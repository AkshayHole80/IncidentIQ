package com.incidentIQ.ai_service.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeIncidentResponseDto {

    private String priority;

    private String category;
}