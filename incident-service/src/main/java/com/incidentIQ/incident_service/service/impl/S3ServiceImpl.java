package com.incidentIQ.incident_service.service.impl;

import com.incidentIQ.incident_service.dto.response.S3UploadResponse;
import com.incidentIQ.incident_service.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public S3UploadResponse uploadFile(
            MultipartFile file) {

        try {

            String key =
                    UUID.randomUUID()
                            + "-"
                            + file.getOriginalFilename();

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(
                                    file.getContentType()
                            )
                            .build(),
                    RequestBody.fromBytes(
                            file.getBytes()
                    )
            );

            String fileUrl =
                    String.format(
                            "https://%s.s3.%s.amazonaws.com/%s",
                            bucketName,
                            "ap-south-1",
                            key
                    );

            return new S3UploadResponse(
                    key,
                    fileUrl
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "File upload failed",
                    e
            );
        }
    }
}