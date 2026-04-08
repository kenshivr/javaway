package com.javaway.user.dto;

import com.javaway.shared.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        Role role,
        LocalDateTime createdAt
) {}
