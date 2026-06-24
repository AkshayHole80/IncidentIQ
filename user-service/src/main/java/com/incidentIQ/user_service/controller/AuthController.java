package com.incidentIQ.user_service.controller;

import com.incidentIQ.user_service.dto.request.LoginRequestDto;
import com.incidentIQ.user_service.dto.request.RegisterRequestDto;
import com.incidentIQ.user_service.dto.response.AuthResponseDto;
import com.incidentIQ.user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid
            @RequestBody RegisterRequestDto request
    ) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @RequestBody LoginRequestDto request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}