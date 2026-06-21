package com.incidentIQ.incident_service.client;

 import com.incidentIQ.incident_service.dto.response.UserResponseDto;
 import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE")
public interface UserServiceClient {

    @GetMapping("/users/email/{email}")
    UserResponseDto getUserByEmail(
            @PathVariable("email") String email
    );
}