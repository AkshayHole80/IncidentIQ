package com.incidentIQ.user_service.controller;

import com.incidentIQ.user_service.dto.request.LoginRequestDto;
import com.incidentIQ.user_service.dto.request.RegisterRequestDto;
import com.incidentIQ.user_service.dto.response.AuthResponseDto;
import com.incidentIQ.user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto register(@Valid
            @RequestBody RegisterRequestDto request
    ) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDto login(
            @RequestBody LoginRequestDto request
    ) {
        return authService.login(request);
    }
}