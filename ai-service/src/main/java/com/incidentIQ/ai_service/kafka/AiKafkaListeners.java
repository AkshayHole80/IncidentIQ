package com.incidentIQ.ai_service.kafka;

import com.incidentIQ.ai_service.dto.request.AnalyzeIncidentRequestDto;
import com.incidentIQ.ai_service.dto.response.AnalyzeIncidentResponseDto;
import com.incidentIQ.ai_service.event.AiClassificationResultEvent;
import com.incidentIQ.ai_service.event.IncidentCreatedEvent;
import com.incidentIQ.ai_service.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiKafkaListeners {

    private final AiService aiService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "incident-created-topic", groupId = "ai-classification-group")
    public void consumeIncidentCreated(IncidentCreatedEvent event) {
        log.info("Received IncidentCreatedEvent for incident id={}", event.getIncidentId());
        try {
            AnalyzeIncidentRequestDto requestDto = new AnalyzeIncidentRequestDto();
            requestDto.setTitle(event.getTitle());
            requestDto.setDescription(event.getDescription());

            log.info("Analyzing incident id={} via AI Service", event.getIncidentId());
            AnalyzeIncidentResponseDto responseDto = aiService.analyzeIncident(requestDto);

            AiClassificationResultEvent resultEvent = AiClassificationResultEvent.builder()
                    .incidentId(event.getIncidentId())
                    .priority(responseDto.getPriority())
                    .category(responseDto.getCategory())
                    .build();

            kafkaTemplate.send("ai-classification-topic", resultEvent);
            log.info("Published AiClassificationResultEvent for incident id={} to Kafka", event.getIncidentId());
        } catch (Exception e) {
            log.error("Error performing AI classification for incident id=" + event.getIncidentId(), e);
        }
    }
}
