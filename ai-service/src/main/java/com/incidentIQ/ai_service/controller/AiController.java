package com.incidentIQ.ai_service.controller;

import com.incidentIQ.ai_service.dto.request.AnalyzeIncidentRequestDto;
import com.incidentIQ.ai_service.dto.response.AnalyzeIncidentResponseDto;
import com.incidentIQ.ai_service.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Controller", description = "Endpoints for invoking AI analysis on incidents")
public class AiController {

    private final AiService aiService;

    @PostMapping("/analyze")
    @Operation(summary = "Analyze incident with AI", description = "Sends incident details to Gemini AI to generate category, priority recommendations, and resolution steps")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI analysis successfully completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request input data"),
            @ApiResponse(responseCode = "503", description = "AI service unavailable: Gemini API call failed")
    })
    public ResponseEntity<AnalyzeIncidentResponseDto> analyzeIncident(
            @RequestBody AnalyzeIncidentRequestDto request) {
        return ResponseEntity.ok(aiService.analyzeIncident(request));
    }
}