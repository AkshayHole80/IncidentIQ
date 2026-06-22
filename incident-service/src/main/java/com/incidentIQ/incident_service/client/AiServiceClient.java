package com.incidentIQ.incident_service.client;

import com.incidentIQ.incident_service.dto.request.AnalyzeIncidentRequestDto;
import com.incidentIQ.incident_service.dto.response.AnalyzeIncidentResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "AI-SERVICE")
public interface AiServiceClient {

    @PostMapping("/api/v1/ai/analyze")
    AnalyzeIncidentResponseDto analyzeIncident(
            @RequestBody AnalyzeIncidentRequestDto request
    );
}