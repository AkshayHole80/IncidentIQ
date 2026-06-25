package com.incidentIQ.incident_service.service;

import com.incidentIQ.incident_service.dto.response.AttachmentResponseDto;
import com.incidentIQ.incident_service.dto.response.AttachmentUrlResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {
    AttachmentResponseDto uploadAttachment(
            Long incidentId,
            MultipartFile file
    );

    List<AttachmentResponseDto>
    getAttachments(
            Long incidentId
    );

    void deleteAttachment(Long attachmentId);

    AttachmentUrlResponseDto getAttachmentViewUrl(Long attachmentId);

    AttachmentUrlResponseDto getAttachmentDownloadUrl(Long attachmentId);
}
