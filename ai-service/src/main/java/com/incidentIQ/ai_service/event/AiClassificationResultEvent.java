package com.incidentIQ.ai_service.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiClassificationResultEvent {
    private Long incidentId;
    private String priority;
    private String category;
}
