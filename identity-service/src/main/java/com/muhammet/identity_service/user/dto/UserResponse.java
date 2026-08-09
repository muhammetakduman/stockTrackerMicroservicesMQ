package com.muhammet.identity_service.user.dto;

import com.muhammet.identity_service.role.entity.RoleName;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        boolean enabled,
        boolean accountLocked,
        Set<RoleName> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

