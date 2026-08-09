package com.muhammet.identity_service.user.dto;

import com.muhammet.identity_service.role.entity.RoleName;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateRolesRequest(
        @NotEmpty(message = "Roles must not be empty")
        Set<RoleName> roles
) {
}

