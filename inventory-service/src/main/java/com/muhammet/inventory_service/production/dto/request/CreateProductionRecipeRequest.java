package com.muhammet.inventory_service.production.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductionRecipeRequest(

        @NotBlank(
                message = "Recipe name cannot be blank"
        )
        String name,

        String description,


        @NotNull(
                message = "Essence stock item ID cannot be null"
        )
        UUID essenceStockItemId,


        @NotNull(
                message = "Bottle stock item ID cannot be null"
        )
        UUID bottleStockItemId,


        @NotNull(
                message = "Packaging set stock item ID cannot be null"
        )
        UUID packagingSetStockItemId,


        @NotNull(
                message = "Output stock item ID cannot be null"
        )
        UUID outputStockItemId,


        @NotNull(
                message = "Essence quantity per unit cannot be null"
        )
        @DecimalMin(
                value = "0.001",
                message = "Essence quantity per unit must be greater than zero"
        )
        BigDecimal essenceQuantityPerUnit,


        @NotNull(
                message = "Bottle quantity per unit cannot be null"
        )
        @DecimalMin(
                value = "1",
                message = "Bottle quantity per unit must be at least one"
        )
        BigDecimal bottleQuantityPerUnit,


        @NotNull(
                message = "Packaging quantity per unit cannot be null"
        )
        @DecimalMin(
                value = "1",
                message = "Packaging quantity per unit must be at least one"
        )
        BigDecimal packagingQuantityPerUnit

) {
}