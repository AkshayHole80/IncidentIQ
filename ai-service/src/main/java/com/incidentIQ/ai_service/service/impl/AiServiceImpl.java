package com.incidentIQ.ai_service.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidentIQ.ai_service.dto.request.AnalyzeIncidentRequestDto;
import com.incidentIQ.ai_service.dto.response.AnalyzeIncidentResponseDto;
import com.incidentIQ.ai_service.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${groq.api-key}")
    private String apiKey;

    @Override
    public AnalyzeIncidentResponseDto analyzeIncident(
            AnalyzeIncidentRequestDto request) {

        String prompt = """
                Analyze this IT incident.

                Title: %s
                Description: %s

                Classify the incident.

                Return ONLY valid JSON.

                {
                  "priority":"LOW|MEDIUM|HIGH|CRITICAL",
                  "category":"APPLICATION|DATABASE|NETWORK|SECURITY|INFRASTRUCTURE"
                }
                """
                .formatted(
                        request.getTitle(),
                        request.getDescription()
                );

        for (int attempt = 1; attempt <= 3; attempt++) {

            try {

                String response = restClient.post()
                        .uri(
                                "https://api.groq.com/openai/v1/chat/completions"
                        )
                        .header(
                                "Authorization",
                                "Bearer " + apiKey
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .body(
                                Map.of(
                                        "model",
                                        "llama-3.3-70b-versatile",
                                        "messages",
                                        new Object[]{
                                                Map.of(
                                                        "role",
                                                        "user",
                                                        "content",
                                                        prompt
                                                )
                                        },
                                        "temperature",
                                        0.1
                                )
                        )
                        .retrieve()
                        .body(String.class);

                JsonNode root =
                        objectMapper.readTree(
                                response
                        );

                String text =
                        root.path("choices")
                                .get(0)
                                .path("message")
                                .path("content")
                                .asText();
                text = text
                        .replace("```json", "")
                        .replace("```", "")
                        .trim();

                log.info("Cleaned AI Response: {}", text);

                AnalyzeIncidentResponseDto aiResponse =
                        objectMapper.readValue(
                                text,
                                AnalyzeIncidentResponseDto.class
                        );

                log.info(
                        "AI Classification -> Priority: {}, Category: {}",
                        aiResponse.getPriority(),
                        aiResponse.getCategory()
                );

                return aiResponse;

            } catch (Exception e) {

                log.warn(
                        "Groq attempt {} failed",
                        attempt,
                        e
                );

                if (attempt < 3) {

                    try {

                        Thread.sleep(
                                2000
                        );

                    } catch (
                            InterruptedException ex) {

                        Thread.currentThread()
                                .interrupt();
                    }
                }
            }
        }

        log.error(
                "Groq unavailable after 3 attempts. Using default classification."
        );

        return AnalyzeIncidentResponseDto.builder()
                .priority("MEDIUM")
                .category("APPLICATION")
                .build();
    }
}