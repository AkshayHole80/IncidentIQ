package com.incidentIQ.incident_service.client;

 import com.incidentIQ.incident_service.dto.response.UserResponseDto;
 import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

 import java.util.List;

@FeignClient(name = "USER-SERVICE")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/email/{email}")
    UserResponseDto getUserByEmail(
            @PathVariable("email") String email
    );

    @GetMapping("/api/v1/users/admins")
    List<UserResponseDto> getAdmins();
}