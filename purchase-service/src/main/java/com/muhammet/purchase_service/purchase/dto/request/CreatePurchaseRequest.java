package com.muhammet.purchase_service.purchase.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreatePurchaseRequest(

        @NotNull(message = "Stock item ID is required")
        UUID stockItemId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        @Digits(
                integer = 16,
                fraction = 3,
                message = "Quantity can contain up to 16 integer and 3 decimal digits"
        )
        BigDecimal quantity,

        @NotNull(message = "Unit price is required")
        @Positive(message = "Unit price must be greater than zero")
        @Digits(
                integer = 15,
                fraction = 4,
                message = "Unit price can contain up to 15 integer and 4 decimal digits"
        )
        BigDecimal unitPrice,

        @NotBlank(message = "Supplier name is required")
        @Size(
                max = 150,
                message = "Supplier name cannot exceed 150 characters"
        )
        String supplierName,

        @PastOrPresent(
                message = "Purchase date cannot be in the future"
        )
        Instant purchasedAt

) {
}
