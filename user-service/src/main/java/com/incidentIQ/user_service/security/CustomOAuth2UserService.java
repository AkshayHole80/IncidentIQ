package com.incidentIQ.user_service.security;

import com.incidentIQ.user_service.entity.User;
import com.incidentIQ.user_service.enums.Role;
import com.incidentIQ.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String email = null;
        String firstName = "";
        String lastName = "";

        if ("google".equalsIgnoreCase(registrationId)) {
            email = (String) attributes.get("email");
            firstName = (String) attributes.get("given_name");
            lastName = (String) attributes.get("family_name");
        } else if ("github".equalsIgnoreCase(registrationId)) {
            email = (String) attributes.get("email");
            if (email == null) {
                email = (String) attributes.get("login") + "@github.com";
            }
            String name = (String) attributes.get("name");
            if (name != null) {
                String[] parts = name.split(" ", 2);
                firstName = parts[0];
                lastName = parts.length > 1 ? parts[1] : "";
            } else {
                firstName = (String) attributes.get("login");
            }
        }

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        // Auto-provision user in DB
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            User newUser = User.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(Role.USER) // Default role
                    .build();
            userRepository.save(newUser);
        }

        return oAuth2User;
    }
}
