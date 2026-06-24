package com.incidentIQ.incident_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponseDto {

    private Long id;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private String contentType;
}
