package com.incidentIQ.incident_service.service.impl;

import com.incidentIQ.incident_service.dto.response.AttachmentResponseDto;
import com.incidentIQ.incident_service.dto.response.AttachmentUrlResponseDto;
import com.incidentIQ.incident_service.dto.response.S3UploadResponse;
import com.incidentIQ.incident_service.entity.Attachment;
import com.incidentIQ.incident_service.entity.Incident;
import com.incidentIQ.incident_service.exception.BadRequestException;
import com.incidentIQ.incident_service.repository.AttachmentRepository;
import com.incidentIQ.incident_service.repository.IncidentRepository;
import com.incidentIQ.incident_service.service.AttachmentService;
import com.incidentIQ.incident_service.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl
        implements AttachmentService {

    private final AttachmentRepository attachmentRepository;

    private final IncidentRepository incidentRepository;

    private final S3Service s3Service;

    @Override
    public AttachmentResponseDto uploadAttachment(
            Long incidentId,
            MultipartFile file) {

        Incident incident =
                incidentRepository.findById(
                                incidentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Incident not found"));

        S3UploadResponse uploadResponse =
                s3Service.uploadFile(file);
        Attachment attachment =
                Attachment.builder()
                        .incidentId(
                                incident.getId()
                        )
                        .fileName(
                                file.getOriginalFilename()
                        )
                        .contentType(
                                file.getContentType()
                        )
                        .fileSize(
                                file.getSize()
                        )
                        .s3Key(
                                uploadResponse.getKey()
                        )
                        .uploadedAt(
                                LocalDateTime.now()
                        )
                        .build();

        Attachment saved =
                attachmentRepository.save(
                        attachment
                );

        return AttachmentResponseDto
                .builder()
                .id(saved.getId())
                .fileName(saved.getFileName())
                .fileSize(saved.getFileSize())
                .contentType(saved.getContentType())
                .build();
    }

    @Override
    public List<AttachmentResponseDto>
    getAttachments(Long incidentId) {

        return attachmentRepository
                .findByIncidentId(
                        incidentId
                )
                .stream()
                .map(a ->
                        AttachmentResponseDto
                                .builder()
                                .id(a.getId())
                                .fileName(
                                        a.getFileName()
                                )
                                .fileSize(
                                        a.getFileSize()
                                )
                                .contentType(
                                        a.getContentType()
                                )
                                .build()
                )
                .toList();
    }

    @Override
    public void deleteAttachment(
            Long attachmentId) {

        Attachment attachment =
                attachmentRepository
                        .findById(attachmentId)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Attachment not found"
                                ));

        s3Service.deleteFile(
                attachment.getS3Key()
        );

        attachmentRepository.delete(
                attachment
        );
    }

    @Override
    public AttachmentUrlResponseDto getAttachmentViewUrl(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BadRequestException("Attachment not found"));
        String url = s3Service.generateViewUrl(attachment.getS3Key());
        return new AttachmentUrlResponseDto(url);
    }

    @Override
    public AttachmentUrlResponseDto getAttachmentDownloadUrl(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BadRequestException("Attachment not found"));
        String url = s3Service.generateDownloadUrl(attachment.getS3Key());
        return new AttachmentUrlResponseDto(url);
    }
}