package com.incidentIQ.user_service.service.impl;

import com.incidentIQ.user_service.constant.CacheNames;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserResponseDto getUserByEmail(String email) {
        log.info("Loading user from PostgreSQL...");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email));

        return  modelMapper.map(user, UserResponseDto.class);
    }

    @Override
    public UserResponseDto getCurrentUser() {

        String email =
                SecurityUtils.getCurrentUserEmail();

        return getUserByEmail(email);
    }
    @Cacheable(CacheNames.SUPPORT_ENGINEERS)
    @Override
    public List<UserResponseDto> getSupportEngineers() {
        log.info("Loading support engineers from PostgreSQL...");
        return userRepository
                .findByRole(Role.SUPPORT_ENGINEER)
                .stream()
                .map(user ->
                        modelMapper.map(
                                user,
                                UserResponseDto.class))
                .collect(Collectors.toList());
    }

    @Cacheable(
            value = CacheNames.USERS_BY_ID,
            key = "#id"
    )
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

    @Cacheable(
            value = CacheNames.ADMINS
    )
    @Override
    public List<UserResponseDto> getAdmins() {
        log.info("Loading admins from PostgreSQL...");
        return userRepository
                .findByRole(Role.ADMIN)
                .stream()
                .map(user ->
                        modelMapper.map(
                                user,
                                UserResponseDto.class))
                .collect(Collectors.toList());
    }
}
