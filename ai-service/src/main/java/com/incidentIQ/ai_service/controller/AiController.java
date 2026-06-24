package com.incidentIQ.ai_service.controller;

import com.incidentIQ.ai_service.dto.request.AnalyzeIncidentRequestDto;
import com.incidentIQ.ai_service.dto.response.AnalyzeIncidentResponseDto;
import com.incidentIQ.ai_service.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeIncidentResponseDto> analyzeIncident(
            @RequestBody AnalyzeIncidentRequestDto request) {
        return ResponseEntity.ok(aiService.analyzeIncident(request));
    }
}