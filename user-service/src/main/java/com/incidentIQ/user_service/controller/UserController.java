package com.incidentIQ.user_service.controller;

import com.incidentIQ.user_service.dto.response.UserResponseDto;
import com.incidentIQ.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/email/{email}")
    public UserResponseDto getUserByEmail(
            @PathVariable String email) {

        return userService.getUserByEmail(email);
    }
    @GetMapping("/me")
    public UserResponseDto getCurrentUser() {

        return userService
                .getCurrentUser();
    }
    @GetMapping("/support-engineers")
    public List<UserResponseDto>
    getSupportEngineers() {

        return userService
                .getSupportEngineers();
    }

    @GetMapping("/{id}")
    public UserResponseDto getUserById(
            @PathVariable Long id) {

        return userService.getUserById(id);
    }
    @GetMapping("/admins")
    public List<UserResponseDto> getAdmins() {

        return userService.getAdmins();
    }
}
