package com.incidentIQ.ai_service.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidentIQ.ai_service.dto.request.AnalyzeIncidentRequestDto;
import com.incidentIQ.ai_service.dto.response.AnalyzeIncidentResponseDto;
import com.incidentIQ.ai_service.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Override
    public AnalyzeIncidentResponseDto analyzeIncident(
            AnalyzeIncidentRequestDto request) {

        String prompt = """
                Analyze this IT incident.

                Title: %s
                Description: %s

                Return ONLY valid JSON:

                {
                  "priority":"LOW|MEDIUM|HIGH|CRITICAL",
                  "category":"APPLICATION|DATABASE|NETWORK|SECURITY|INFRASTRUCTURE"
                }
                """
                .formatted(
                        request.getTitle(),
                        request.getDescription()
                );

        String response = restClient.post()
                .uri(
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent"
                )
                .header("X-goog-api-key", apiKey)
                .body(
                        Map.of(
                                "contents",
                                new Object[]{
                                        Map.of(
                                                "parts",
                                                new Object[]{
                                                        Map.of("text", prompt)
                                                }
                                        )
                                }
                        )
                )
                .retrieve()
                .body(String.class);

        try {

            JsonNode root =
                    objectMapper.readTree(response);

            String text =
                    root.path("candidates")
                            .get(0)
                            .path("content")
                            .path("parts")
                            .get(0)
                            .path("text")
                            .asText();

            return objectMapper.readValue(
                    text,
                    AnalyzeIncidentResponseDto.class
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to parse Gemini response",
                    e
            );
        }
    }
}