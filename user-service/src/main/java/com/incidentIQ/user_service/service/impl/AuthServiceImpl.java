package com.incidentIQ.user_service.service.impl;

import com.incidentIQ.user_service.dto.request.LoginRequestDto;
import com.incidentIQ.user_service.dto.request.RegisterRequestDto;
import com.incidentIQ.user_service.dto.response.AuthResponseDto;
import com.incidentIQ.user_service.entity.User;
import com.incidentIQ.user_service.exception.UserAlreadyExistsException;
import com.incidentIQ.user_service.repository.UserRepository;
import com.incidentIQ.user_service.security.CustomUserDetails;
import com.incidentIQ.user_service.security.JwtService;
import com.incidentIQ.user_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponseDto register(RegisterRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User already exists with email: "
                            + request.getEmail()
            );
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(
                new CustomUserDetails(user)
        );

        return AuthResponseDto.builder()
                .token(token)
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponseDto login(LoginRequestDto request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String token = jwtService.generateToken(
                new CustomUserDetails(user)
        );

        return AuthResponseDto.builder()
                .token(token)
                .role(user.getRole().name())
                .build();
    }
}