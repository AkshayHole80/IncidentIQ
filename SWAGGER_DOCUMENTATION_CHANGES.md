# Swagger/OpenAPI Documentation Changes

This document provides a summary of all changes and details every file modified/added to configure and document the APIs using Swagger/OpenAPI across all business microservices.

## Summary of Changes
1. **Dependencies Added**: Added the `springdoc-openapi-starter-webmvc-ui` dependency to the `pom.xml` files of User Service, Incident Service, Notification Service, and AI Service.
2. **Security Setup**: Configured Spring Security in `user-service` and `incident-service` to permit public access to all Swagger UI and OpenAPI documentation resources (`/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`).
3. **OpenAPI Configuration**: Created custom `OpenApiConfig.java` beans in all 4 microservices to specify title, description, version, and enable JWT Bearer token authentication schema in the Swagger UI.
4. **Controller Annotations**: Documented all endpoint operations, controller descriptions, and success/error HTTP response codes in every API controller.

---

## File Modifications

### 1. User Service pom.xml
Package:
`user-service`
File:
`pom.xml`
Reason:
Added Springdoc OpenAPI UI dependency.
Code Added/Modified:
```xml
		<dependency>
			<groupId>org.springdoc</groupId>
			<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
			<version>2.8.5</version>
		</dependency>
```

---

### 2. Incident Service pom.xml
Package:
`incident-service`
File:
`pom.xml`
Reason:
Added Springdoc OpenAPI UI dependency.
Code Added/Modified:
```xml
		<dependency>
			<groupId>org.springdoc</groupId>
			<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
			<version>2.8.5</version>
		</dependency>
```

---

### 3. Notification Service pom.xml
Package:
`notification-service`
File:
`pom.xml`
Reason:
Added Springdoc OpenAPI UI dependency.
Code Added/Modified:
```xml
		<dependency>
			<groupId>org.springdoc</groupId>
			<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
			<version>2.8.5</version>
		</dependency>
```

---

### 4. AI Service pom.xml
Package:
`ai-service`
File:
`pom.xml`
Reason:
Added Springdoc OpenAPI UI dependency.
Code Added/Modified:
```xml
		<dependency>
			<groupId>org.springdoc</groupId>
			<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
			<version>2.8.5</version>
		</dependency>
```

---

### 5. User Service SecurityConfig.java
Package:
`com.incidentIQ.user_service.security`
File:
`SecurityConfig.java`
Reason:
Permitted public access to Swagger and OpenAPI documentation routes.
Code Added/Modified:
```java
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/users/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
```

---

### 6. Incident Service SecurityConfig.java
Package:
`com.incidentIQ.incident_service.security`
File:
`SecurityConfig.java`
Reason:
Permitted public access to Swagger and OpenAPI documentation routes.
Code Added/Modified:
```java
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest()
                        .authenticated()
                )
```

---

### 7. User Service OpenApiConfig.java
Package:
`com.incidentIQ.user_service.config`
File:
`OpenApiConfig.java`
Reason:
Added Swagger/OpenAPI configuration with User Service metadata and JWT Bearer scheme.
Code Added/Modified:
```java
package com.incidentIQ.user_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .description("API Documentation for IncidentIQ User Service")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
```

---

### 8. Incident Service OpenApiConfig.java
Package:
`com.incidentIQ.incident_service.config`
File:
`OpenApiConfig.java`
Reason:
Added Swagger/OpenAPI configuration with Incident Service metadata and JWT Bearer scheme.
Code Added/Modified:
```java
package com.incidentIQ.incident_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Incident Service API")
                        .description("API Documentation for IncidentIQ Incident Service")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
```

---

### 9. Notification Service OpenApiConfig.java
Package:
`com.incidentIQ.notification_service.config`
File:
`OpenApiConfig.java`
Reason:
Added Swagger/OpenAPI configuration with Notification Service metadata and JWT Bearer scheme.
Code Added/Modified:
```java
package com.incidentIQ.notification_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .description("API Documentation for IncidentIQ Notification Service")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
```

---

### 10. AI Service OpenApiConfig.java
Package:
`com.incidentIQ.ai_service.config`
File:
`OpenApiConfig.java`
Reason:
Added Swagger/OpenAPI configuration with AI Service metadata and JWT Bearer scheme.
Code Added/Modified:
```java
package com.incidentIQ.ai_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("AI Service API")
                        .description("API Documentation for IncidentIQ AI Service")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
```

---

### 11. User Service UserController.java
Package:
`com.incidentIQ.user_service.controller`
File:
`UserController.java`
Reason:
Added Swagger class-level tag and endpoint operations/responses documentation.
Code Added/Modified:
```java
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
```

---

### 12. User Service AuthController.java
Package:
`com.incidentIQ.user_service.controller`
File:
`AuthController.java`
Reason:
Added Swagger class-level tag and endpoint operations/responses documentation.
Code Added/Modified:
```java
package com.incidentIQ.user_service.controller;

import com.incidentIQ.user_service.dto.request.LoginRequestDto;
import com.incidentIQ.user_service.dto.request.RegisterRequestDto;
import com.incidentIQ.user_service.dto.response.AuthResponseDto;
import com.incidentIQ.user_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller", description = "Endpoints for user registration and authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user profile in the system")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid registration data or email already taken")
    })
    public ResponseEntity<AuthResponseDto> register(@Valid
            @RequestBody RegisterRequestDto request
    ) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Logs in a user and returns a JWT access token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully authenticated"),
            @ApiResponse(responseCode = "400", description = "Invalid input format"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponseDto> login(
            @RequestBody LoginRequestDto request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

---

### 13. Incident Service IncidentController.java
Package:
`com.incidentIQ.incident_service.controller`
File:
`IncidentController.java`
Reason:
Added Swagger class-level tag and decorated all endpoint operations, parameters, and responses.
Code Added/Modified:
```java
package com.incidentIQ.incident_service.controller;

import com.incidentIQ.incident_service.dto.request.AssignIncidentRequestDto;
import com.incidentIQ.incident_service.dto.request.CreateIncidentRequestDto;
import com.incidentIQ.incident_service.dto.request.ResolveIncidentRequestDto;
import com.incidentIQ.incident_service.dto.request.UpdateIncidentRequestDto;
import com.incidentIQ.incident_service.dto.response.AuditLogResponseDto;
import com.incidentIQ.incident_service.dto.response.IncidentResponseDto;
import com.incidentIQ.incident_service.dto.response.IncidentStatsResponseDto;
import com.incidentIQ.incident_service.enums.Category;
import com.incidentIQ.incident_service.enums.IncidentStatus;
import com.incidentIQ.incident_service.enums.Priority;
import com.incidentIQ.incident_service.service.AuditLogService;
import com.incidentIQ.incident_service.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
@Tag(name = "Incident Controller", description = "Endpoints for reporting, assigning, updating, and tracking incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final AuditLogService auditLogService;

    @PostMapping
    @Operation(summary = "Create a new incident", description = "Creates a new system incident reported by a user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Incident successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<IncidentResponseDto> createIncident(
            @Valid @RequestBody CreateIncidentRequestDto request) {
        return new ResponseEntity<>(incidentService.createIncident(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get incident by ID", description = "Fetches an incident profile by its database ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incident found successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponseDto> getIncidentById(
            @PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getIncidentById(id));
    }

    @GetMapping
    @Operation(summary = "Get all incidents", description = "Retrieves all incidents globally in the system (Admins receive all, Support/Users see authorized ones)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incidents list retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<IncidentResponseDto>> getAllIncidents() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an incident", description = "Updates general attributes of an incident")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incident updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponseDto> updateIncident(
            @PathVariable Long id,
            @RequestBody UpdateIncidentRequestDto request) {
        return ResponseEntity.ok(incidentService.updateIncident(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an incident", description = "Removes an incident from the system")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Incident deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Forbidden action"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<Void> deleteIncident(
            @PathVariable Long id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Assign incident to engineer", description = "Assigns a reported incident to a support engineer (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incident assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Forbidden: only Admins can assign incidents"),
            @ApiResponse(responseCode = "404", description = "Incident or engineer not found")
    })
    public ResponseEntity<IncidentResponseDto> assignIncident(
            @PathVariable Long id,
            @RequestBody AssignIncidentRequestDto request) {
        return ResponseEntity.ok(incidentService.assignIncident(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update incident status", description = "Changes the lifecycle status of an incident")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incident status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request status value"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Forbidden action"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponseDto> updateStatus(
            @PathVariable Long id,
            @RequestParam IncidentStatus status) {
        return ResponseEntity.ok(incidentService.updateStatus(id, status));
    }

    @GetMapping("/assigned")
    @Operation(summary = "Get incidents assigned to current user", description = "Retrieves all incidents assigned to the logged-in engineer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assigned incidents list retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<IncidentResponseDto>> getAssignedIncidents() {
        return ResponseEntity.ok(incidentService.getAssignedIncidents());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get incidents by status", description = "Retrieves all incidents filtered by a specific status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of incidents returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<IncidentResponseDto>> getByStatus(
            @PathVariable IncidentStatus status) {
        return ResponseEntity.ok(incidentService.getIncidentsByStatus(status));
    }

    @GetMapping("/priority/{priority}")
    @Operation(summary = "Get incidents by priority", description = "Retrieves all incidents filtered by priority level")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of incidents returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<IncidentResponseDto>> getByPriority(
            @PathVariable Priority priority) {
        return ResponseEntity.ok(incidentService.getIncidentsByPriority(priority));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get incidents by category", description = "Retrieves all incidents filtered by category name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of incidents returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<IncidentResponseDto>> getByCategory(
            @PathVariable Category category) {
        return ResponseEntity.ok(incidentService.getIncidentsByCategory(category));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get role-aware dashboard stats", description = "Retrieves incident stats counts customized based on user role (Admin: global, Support: assigned, User: owned)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stats object returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<IncidentStatsResponseDto> getStats() {
        return ResponseEntity.ok(incidentService.getIncidentStats());
    }

    @GetMapping("/search")
    @Operation(summary = "Search incidents by keyword", description = "Searches for incidents whose title or description contains the keyword")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results list returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<IncidentResponseDto>> searchIncidents(
            @RequestParam String keyword) {
        return ResponseEntity.ok(incidentService.searchIncidents(keyword));
    }

    @PatchMapping("/{id}/resolve")
    @Operation(summary = "Resolve an incident", description = "Marks an incident as RESOLVED with resolution notes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incident successfully resolved"),
            @ApiResponse(responseCode = "400", description = "Invalid request resolution notes"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Forbidden action"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponseDto> resolveIncident(
            @PathVariable Long id,
            @RequestBody ResolveIncidentRequestDto request) {
        return ResponseEntity.ok(incidentService.resolveIncident(id, request));
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Close an incident", description = "Marks a resolved incident as CLOSED (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incident successfully closed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Forbidden: only Admins can close incidents"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponseDto> closeIncident(
            @PathVariable Long id) {
        return ResponseEntity.ok(incidentService.closeIncident(id));
    }

    @GetMapping("/{id}/audit-logs")
    @Operation(summary = "Get incident audit logs", description = "Retrieves historical audit trail/logs for a specific incident")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit logs list returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<List<AuditLogResponseDto>> getAuditLogs(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(auditLogService.getAuditLogs(id));
    }
}
```

---

### 14. Incident Service AttachmentController.java
Package:
`com.incidentIQ.incident_service.controller`
File:
`AttachmentController.java`
Reason:
Added Swagger class-level tag and decorated all attachment uploading/deleting endpoint operations.
Code Added/Modified:
```java
package com.incidentIQ.incident_service.controller;

import com.incidentIQ.incident_service.dto.response.AttachmentResponseDto;
import com.incidentIQ.incident_service.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/incidents")
@Tag(name = "Attachment Controller", description = "Endpoints for uploading, viewing, and deleting incident attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(
            value = "/{incidentId}/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Upload incident attachment", description = "Uploads a supporting document or screenshot for an incident")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Attachment uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request file or parameter data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<AttachmentResponseDto> uploadAttachment(
            @PathVariable Long incidentId,
            @RequestParam("file") MultipartFile file
    ) {
        return new ResponseEntity<>(attachmentService.uploadAttachment(incidentId, file), HttpStatus.CREATED);
    }

    @GetMapping("/{incidentId}/attachments")
    @Operation(summary = "Get incident attachments", description = "Retrieves all attachments associated with a specific incident")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of attachments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<List<AttachmentResponseDto>> getAttachments(
            @PathVariable Long incidentId
    ) {
        return ResponseEntity.ok(attachmentService.getAttachments(incidentId));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    @Operation(summary = "Delete attachment", description = "Deletes an attachment from S3 and the database (Allowed only when incident is OPEN)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Attachment deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to delete attachment: forbidden status or database state error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Attachment not found")
    })
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long attachmentId
    ) {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }
}
```

---

### 15. Notification Service NotificationController.java
Package:
`com.incidentIQ.notification_service.controller`
File:
`NotificationController.java`
Reason:
Added Swagger class-level tag and decorated all notifications endpoints.
Code Added/Modified:
```java
package com.incidentIQ.notification_service.controller;

import com.incidentIQ.notification_service.dto.request.CreateNotificationRequestDto;
import com.incidentIQ.notification_service.dto.response.NotificationResponseDto;
import com.incidentIQ.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "Endpoints for creating and retrieving system notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create system notification", description = "Creates and sends a system-wide or user-specific notification")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notification successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request input data")
    })
    public ResponseEntity<NotificationResponseDto> createNotification(
            @RequestBody CreateNotificationRequestDto request) {
        return new ResponseEntity<>(notificationService.createNotification(request), HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notifications for user", description = "Retrieves all notifications associated with a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications list returned successfully")
    })
    public ResponseEntity<List<NotificationResponseDto>> getNotificationsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUser(userId));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Updates a notification status to read")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification marked as read successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/unread-count")
    @Operation(summary = "Get unread notifications count", description = "Retrieves the count of unread notifications for a specific user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unread notifications count returned successfully")
    })
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable Long userId) {
        return ResponseEntity.ok(Map.of(
                "count",
                notificationService.getUnreadCount(userId)
        ));
    }
}
```

---

### 16. AI Service AiController.java
Package:
`com.incidentIQ.ai_service.controller`
File:
`AiController.java`
Reason:
Added Swagger class-level tag and decorated AI analysis endpoint.
Code Added/Modified:
```java
package com.incidentIQ.ai_service.controller;

import com.incidentIQ.ai_service.dto.request.AnalyzeIncidentRequestDto;
import com.incidentIQ.ai_service.dto.response.AnalyzeIncidentResponseDto;
import com.incidentIQ.ai_service.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Controller", description = "Endpoints for invoking AI analysis on incidents")
public class AiController {

    private final AiService aiService;

    @PostMapping("/analyze")
    @Operation(summary = "Analyze incident with AI", description = "Sends incident details to Gemini AI to generate category, priority recommendations, and resolution steps")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI analysis successfully completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request input data"),
            @ApiResponse(responseCode = "503", description = "AI service unavailable: Gemini API call failed")
    })
    public ResponseEntity<AnalyzeIncidentResponseDto> analyzeIncident(
            @RequestBody AnalyzeIncidentRequestDto request) {
        return ResponseEntity.ok(aiService.analyzeIncident(request));
    }
}
```
