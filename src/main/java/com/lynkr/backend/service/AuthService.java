package com.lynkr.backend.service;

import com.lynkr.backend.dto.AuthResponse;
import com.lynkr.backend.dto.LoginRequest;
import com.lynkr.backend.dto.RegisterRequest;
import com.lynkr.backend.dto.UserDto;
import com.lynkr.backend.exception.BadRequestException;
import com.lynkr.backend.model.User;
import com.lynkr.backend.repository.UserRepository;
import com.lynkr.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered!");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserDto(savedUser))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password!");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserDto(user))
                .build();
    }

    public UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
