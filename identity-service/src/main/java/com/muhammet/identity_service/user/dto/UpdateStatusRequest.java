package com.muhammet.identity_service.user.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "Enabled status must not be null")
        Boolean enabled
) {
}

