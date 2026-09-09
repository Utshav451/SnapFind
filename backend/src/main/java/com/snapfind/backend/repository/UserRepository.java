package com.snapfind.backend.repository;

import com.snapfind.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //Used during login — find user by email
    Optional<User> findByEmail(String email);

    //Used during register — check if email already taken
    boolean existsByEmail(String email);
}
