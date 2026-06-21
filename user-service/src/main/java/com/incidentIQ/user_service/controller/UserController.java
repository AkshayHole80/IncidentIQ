package com.incidentIQ.user_service.controller;

import com.incidentIQ.user_service.dto.response.UserResponseDto;
import com.incidentIQ.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/email/{email}")
    public UserResponseDto getUserByEmail(
            @PathVariable String email) {

        return userService.getUserByEmail(email);
    }
}
