package com.zs.spring_security_jwt.dto;

public record LoginRequest(
        String email,
        String password
) {}
