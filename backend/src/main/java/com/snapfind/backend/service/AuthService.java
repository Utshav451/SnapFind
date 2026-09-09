package com.snapfind.backend.service;

import com.snapfind.backend.config.JwtUtil;
import com.snapfind.backend.dto.AuthResponse;
import com.snapfind.backend.dto.LoginRequest;
import com.snapfind.backend.dto.RegisterRequest;
import com.snapfind.backend.entity.User;
import com.snapfind.backend.exception.BadRequestException;
import com.snapfind.backend.exception.DuplicateResourceException;
import com.snapfind.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {

        //Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        //Build new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        //Save to DB
        User savedUser = userRepository.save(user);

        //Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getId());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }

    public AuthResponse login(LoginRequest request) {

        //Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        //Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        //Generate JWT token
        String token = jwtUtil.generateToken(user.getId());

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
