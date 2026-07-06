package com.incidentIQ.notification_service.controller;

import com.incidentIQ.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/test")
    public ResponseEntity<String> sendTestEmail(
            @RequestParam String to) {

        return emailService.sendTestEmail(to);
    }
}