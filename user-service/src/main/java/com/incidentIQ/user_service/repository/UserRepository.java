package com.incidentIQ.user_service.repository;

 import com.incidentIQ.user_service.entity.User;
 import com.incidentIQ.user_service.enums.Role;
 import org.springframework.data.jpa.repository.JpaRepository;

 import java.util.Arrays;
 import java.util.List;
 import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

}