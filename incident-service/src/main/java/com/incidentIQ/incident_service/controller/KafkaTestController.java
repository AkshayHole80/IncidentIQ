package com.incidentIQ.incident_service.controller;

import com.incidentIQ.incident_service.event.IncidentNotificationEvent;
import com.incidentIQ.incident_service.kafka.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class KafkaTestController {

    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/kafka")
    public String testKafka() {

        kafkaProducerService.sendNotification(
                IncidentNotificationEvent.builder()
                        .userId(5L)
                        .title("Kafka Test")
                        .message(
                                "Testing Kafka Producer"
                        )
                        .build()
        );

        return "Event Sent";
    }
}