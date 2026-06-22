package com.incidentIQ.ai_service.service;

import com.incidentIQ.ai_service.dto.request.AnalyzeIncidentRequestDto;
import com.incidentIQ.ai_service.dto.response.AnalyzeIncidentResponseDto;

public interface AiService {

    AnalyzeIncidentResponseDto analyzeIncident(
            AnalyzeIncidentRequestDto request);
}