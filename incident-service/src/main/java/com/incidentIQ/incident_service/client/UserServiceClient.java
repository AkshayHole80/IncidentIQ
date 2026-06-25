package com.incidentIQ.incident_service.client;

 import com.incidentIQ.incident_service.dto.response.UserResponseDto;
 import com.incidentIQ.incident_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

 import java.util.List;

@FeignClient(name = "USER-SERVICE", configuration = FeignConfig.class)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/email/{email}")
    UserResponseDto getUserByEmail(
            @PathVariable("email") String email
    );

    @GetMapping("/api/v1/users/admins")
    List<UserResponseDto> getAdmins();
}