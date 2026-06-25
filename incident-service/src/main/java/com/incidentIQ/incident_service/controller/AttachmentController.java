package com.incidentIQ.incident_service.controller;

import com.incidentIQ.incident_service.dto.response.AttachmentResponseDto;
import com.incidentIQ.incident_service.dto.response.AttachmentUrlResponseDto;
import com.incidentIQ.incident_service.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Attachment Controller", description = "Endpoints for uploading, viewing, and deleting incident attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(
            value = "/{incidentId}/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Upload incident attachment", description = "Uploads a supporting document or screenshot for an incident")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Attachment uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request file or parameter data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<AttachmentResponseDto> uploadAttachment(
            @PathVariable Long incidentId,
            @RequestParam("file") MultipartFile file
    ) {
        return new ResponseEntity<>(attachmentService.uploadAttachment(incidentId, file), HttpStatus.CREATED);
    }

    @GetMapping("/{incidentId}/attachments")
    @Operation(summary = "Get incident attachments", description = "Retrieves all attachments associated with a specific incident")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of attachments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<List<AttachmentResponseDto>> getAttachments(
            @PathVariable Long incidentId
    ) {
        return ResponseEntity.ok(attachmentService.getAttachments(incidentId));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    @Operation(summary = "Delete attachment", description = "Deletes an attachment from S3 and the database (Allowed only when incident is OPEN)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Attachment deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to delete attachment: forbidden status or database state error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Attachment not found")
    })
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long attachmentId
    ) {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/attachments/{attachmentId}/view")
    @Operation(summary = "Get pre-signed view URL", description = "Generates a temporary pre-signed URL for viewing an attachment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pre-signed view URL generated successfully"),
            @ApiResponse(responseCode = "400", description = "Attachment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<AttachmentUrlResponseDto> getAttachmentViewUrl(
            @PathVariable Long attachmentId
    ) {
        return ResponseEntity.ok(attachmentService.getAttachmentViewUrl(attachmentId));
    }

    @GetMapping("/attachments/{attachmentId}/download")
    @Operation(summary = "Get pre-signed download URL", description = "Generates a temporary pre-signed URL for downloading an attachment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pre-signed download URL generated successfully"),
            @ApiResponse(responseCode = "400", description = "Attachment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<AttachmentUrlResponseDto> getAttachmentDownloadUrl(
            @PathVariable Long attachmentId
    ) {
        return ResponseEntity.ok(attachmentService.getAttachmentDownloadUrl(attachmentId));
    }
}