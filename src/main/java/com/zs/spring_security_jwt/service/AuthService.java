package com.zs.spring_security_jwt.service;

import com.zs.spring_security_jwt.dto.AuthResponse;
import com.zs.spring_security_jwt.dto.LoginRequest;
import com.zs.spring_security_jwt.dto.RefreshTokenRequest;
import com.zs.spring_security_jwt.dto.RegisterRequest;
import com.zs.spring_security_jwt.entity.Role;
import com.zs.spring_security_jwt.entity.User;
import com.zs.spring_security_jwt.jwt.JwtService;
import com.zs.spring_security_jwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);
        log.info("User registered: {}", user.getEmail());

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.of(accessToken, refreshToken, user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        log.info("User logged in: {}", user.getEmail());

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.of(accessToken, refreshToken, user.getEmail(), user.getRole().name());
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        final String userEmail = jwtService.extractUsername(request.refreshToken());

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!jwtService.isTokenValid(request.refreshToken(), user)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        log.info("Token refreshed for: {}", user.getEmail());

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.of(newAccessToken, newRefreshToken, user.getEmail(), user.getRole().name());
    }
}
