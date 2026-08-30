package com.muhammet.sales_service.sale.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateSaleRequest(

        @NotNull
        UUID customerId,

        @NotNull
        UUID stockItemId,

        @NotNull
        @DecimalMin("0.001")
        BigDecimal quantity,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal unitPrice,

        @NotNull
        Instant soldAt,

        @Size(max = 1000)
        String note

) {
}