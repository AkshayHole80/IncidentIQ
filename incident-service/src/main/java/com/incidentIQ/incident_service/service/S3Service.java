package com.incidentIQ.incident_service.service;

import com.incidentIQ.incident_service.dto.response.S3UploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    S3UploadResponse uploadFile(MultipartFile file);
    void deleteFile(String key);

}