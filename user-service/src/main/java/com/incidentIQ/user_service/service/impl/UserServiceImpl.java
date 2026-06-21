package com.incidentIQ.user_service.service.impl;

import com.incidentIQ.user_service.dto.response.UserResponseDto;
import com.incidentIQ.user_service.entity.User;
import com.incidentIQ.user_service.exception.ResourceNotFoundException;
import com.incidentIQ.user_service.repository.UserRepository;
import com.incidentIQ.user_service.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    @Override
    public UserResponseDto getUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email));

        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
