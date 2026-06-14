package com.zs.spring_security_jwt.service;

import com.zs.spring_security_jwt.dto.AuthResponse;
import com.zs.spring_security_jwt.dto.LoginRequest;
import com.zs.spring_security_jwt.dto.RefreshTokenRequest;
import com.zs.spring_security_jwt.dto.RegisterRequest;
import com.zs.spring_security_jwt.entity.Role;
import com.zs.spring_security_jwt.entity.User;
import com.zs.spring_security_jwt.jwt.JwtService;
import com.zs.spring_security_jwt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private final User savedUser = User.builder()
            .id(UUID.randomUUID())
            .email("joao@email.com")
            .name("João")
            .password("encoded")
            .role(Role.ROLE_USER)
            .build();

    @Test
    void shouldRegisterUserAndReturnTokens() {
        RegisterRequest request = new RegisterRequest("João", "joao@email.com", "123456");

        when(userRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.email()).isEqualTo("joao@email.com");
        assertThat(response.role()).isEqualTo("ROLE_USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowWhenEmailAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest("João", "joao@email.com", "123456");
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void shouldLoginAndReturnTokens() {
        LoginRequest request = new LoginRequest("joao@email.com", "123456");

        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(savedUser));
        when(jwtService.generateToken(savedUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(savedUser)).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldRefreshTokenAndReturnNewTokens() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

        when(jwtService.extractUsername("valid-refresh-token")).thenReturn("joao@email.com");
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(savedUser));
        when(jwtService.isTokenValid("valid-refresh-token", savedUser)).thenReturn(true);
        when(jwtService.generateToken(savedUser)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(savedUser)).thenReturn("new-refresh-token");

        AuthResponse response = authService.refreshToken(request);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void shouldThrowWhenRefreshTokenInvalid() {
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");

        when(jwtService.extractUsername("invalid-token")).thenReturn("joao@email.com");
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(savedUser));
        when(jwtService.isTokenValid("invalid-token", savedUser)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid refresh token");
    }
}
