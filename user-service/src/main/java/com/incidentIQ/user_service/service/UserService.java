package com.incidentIQ.user_service.service;

import com.incidentIQ.user_service.dto.response.UserResponseDto;

import java.util.List;

public interface UserService {
    UserResponseDto getUserByEmail(String email);
    UserResponseDto getCurrentUser();
    List<UserResponseDto> getSupportEngineers();
}
