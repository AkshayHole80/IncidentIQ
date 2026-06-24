package com.incidentIQ.incident_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RestController
@RequiredArgsConstructor
public class S3TestController {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @GetMapping("/api/v1/test/s3")
    public String test() {

        try {

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key("test.txt")
                            .build(),
                    RequestBody.fromString(
                            "IncidentIQ AWS Test"
                    )
            );

            return "Upload Successful";

        } catch (Exception e) {
            return e.getMessage();
        }
    }
}