package com.zs.spring_security_jwt.dto;

public record RegisterRequest(
        String name,
        String email,
        String password
) {}
