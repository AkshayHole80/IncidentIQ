package com.incidentIQ.incident_service.controller;

import com.incidentIQ.incident_service.dto.response.AttachmentResponseDto;
import com.incidentIQ.incident_service.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/incidents")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(
            value = "/{incidentId}/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<AttachmentResponseDto> uploadAttachment(
            @PathVariable Long incidentId,
            @RequestParam("file") MultipartFile file
    ) {
        return new ResponseEntity<>(attachmentService.uploadAttachment(incidentId, file), HttpStatus.CREATED);
    }

    @GetMapping("/{incidentId}/attachments")
    public ResponseEntity<List<AttachmentResponseDto>> getAttachments(
            @PathVariable Long incidentId
    ) {
        return ResponseEntity.ok(attachmentService.getAttachments(incidentId));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long attachmentId
    ) {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }
}