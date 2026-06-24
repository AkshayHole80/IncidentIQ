package com.incidentIQ.user_service.controller;

import com.incidentIQ.user_service.dto.response.UserResponseDto;
import com.incidentIQ.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Controller", description = "Endpoints for managing user accounts and retrieving profiles")
public class UserController {

    private final UserService userService;

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Fetches a user profile by their email address")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile found successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponseDto> getUserByEmail(
            @PathVariable String email) {

        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user", description = "Retrieves the logged-in user profile from context")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current user retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<UserResponseDto> getCurrentUser() {

        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @GetMapping("/support-engineers")
    @Operation(summary = "Get support engineers list", description = "Retrieves all users registered as support engineers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of support engineers returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<UserResponseDto>> getSupportEngineers() {

        return ResponseEntity.ok(userService.getSupportEngineers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Fetches a user profile by their database ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile found successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponseDto> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/admins")
    @Operation(summary = "Get admins list", description = "Retrieves all users registered as administrators")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of administrators returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<UserResponseDto>> getAdmins() {

        return ResponseEntity.ok(userService.getAdmins());
    }
}
