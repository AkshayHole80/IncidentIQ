package com.incidentIQ.user_service.service;

import com.incidentIQ.user_service.dto.response.UserResponseDto;

public interface UserService {
    UserResponseDto getUserByEmail(String email);
}
