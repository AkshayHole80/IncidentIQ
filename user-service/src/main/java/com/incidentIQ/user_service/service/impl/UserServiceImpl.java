package com.incidentIQ.user_service.service.impl;

import com.incidentIQ.user_service.dto.response.UserResponseDto;
import com.incidentIQ.user_service.entity.User;
import com.incidentIQ.user_service.enums.Role;
import com.incidentIQ.user_service.exception.ResourceNotFoundException;
import com.incidentIQ.user_service.repository.UserRepository;
import com.incidentIQ.user_service.service.UserService;

import com.incidentIQ.user_service.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Cacheable(
            value = "usersByEmail",
            key = "#email"
    )
    @Override
    public UserResponseDto getUserByEmail(String email) {
        log.info("Loading user from PostgreSQL...");
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

    @Override
    public UserResponseDto getCurrentUser() {

        String email =
                SecurityUtils.getCurrentUserEmail();

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                ));

        return modelMapper.map(
                user,
                UserResponseDto.class
        );
    }

    @Override
    public List<UserResponseDto> getSupportEngineers() {

        return userRepository
                .findByRole(Role.SUPPORT_ENGINEER)
                .stream()
                .map(user ->
                        modelMapper.map(
                                user,
                                UserResponseDto.class))
                .toList();
    }
    @Override
    public UserResponseDto getUserById(Long id) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        ));

        return modelMapper.map(
                user,
                UserResponseDto.class
        );
    }
    @Override
    public List<UserResponseDto> getAdmins() {

        return userRepository
                .findByRole(Role.ADMIN)
                .stream()
                .map(user ->
                        modelMapper.map(
                                user,
                                UserResponseDto.class))
                .toList();
    }
}
