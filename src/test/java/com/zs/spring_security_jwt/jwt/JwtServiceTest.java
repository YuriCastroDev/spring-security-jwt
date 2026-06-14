package com.zs.spring_security_jwt.jwt;

import com.zs.spring_security_jwt.entity.Role;
import com.zs.spring_security_jwt.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L);

        user = User.builder()
                .id(UUID.randomUUID())
                .email("joao@email.com")
                .password("encoded-password")
                .name("João")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    void shouldGenerateToken() {
        String token = jwtService.generateToken(user);
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtService.generateToken(user);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("joao@email.com");
    }

    @Test
    void shouldValidateTokenForCorrectUser() {
        String token = jwtService.generateToken(user);
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void shouldNotValidateTokenForWrongUser() {
        String token = jwtService.generateToken(user);

        User anotherUser = User.builder()
                .email("outro@email.com")
                .password("pass")
                .role(Role.ROLE_USER)
                .build();

        assertThat(jwtService.isTokenValid(token, anotherUser)).isFalse();
    }

    @Test
    void shouldGenerateRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken(user);
        assertThat(refreshToken).isNotNull().isNotEmpty();
        assertThat(jwtService.extractUsername(refreshToken)).isEqualTo("joao@email.com");
    }

    @Test
    void shouldNotConsiderValidTokenAsExpired() {
        String token = jwtService.generateToken(user);
        assertThat(jwtService.isTokenExpired(token)).isFalse();
    }

    @Test
    void shouldThrowForInvalidToken() {
        assertThatThrownBy(() -> jwtService.extractUsername("invalid.token.here"))
                .isInstanceOf(Exception.class);
    }
}
