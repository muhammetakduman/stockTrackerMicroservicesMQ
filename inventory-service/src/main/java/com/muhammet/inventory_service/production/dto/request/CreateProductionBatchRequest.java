package com.muhammet.inventory_service.production.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductionBatchRequest(

        @NotNull(
                message = "Operation ID cannot be null"
        )
        UUID operationId,

        @NotNull(
                message = "Recipe ID cannot be null"
        )
        UUID recipeId,

        @NotNull(
                message = "Output quantity cannot be null"
        )
        @DecimalMin(
                value = "1",
                message = "Output quantity must be at least one"
        )
        BigDecimal outputQuantity,

        String note

) {
}