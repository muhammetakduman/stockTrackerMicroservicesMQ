package com.muhammet.inventory_service.production.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateProductionRecipeStatusRequest(

        @NotNull(
                message = "Active status cannot be null"
        )
        Boolean active

) {
}