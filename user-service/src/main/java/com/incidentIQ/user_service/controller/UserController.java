package com.incidentIQ.user_service.controller;

import com.incidentIQ.user_service.dto.response.UserResponseDto;
import com.incidentIQ.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UserResponseDto> getUserByEmail(
            @PathVariable String email) {

        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser() {

        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @GetMapping("/support-engineers")
    public ResponseEntity<List<UserResponseDto>> getSupportEngineers() {

        return ResponseEntity.ok(userService.getSupportEngineers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/admins")
    public ResponseEntity<List<UserResponseDto>> getAdmins() {

        return ResponseEntity.ok(userService.getAdmins());
    }
}
