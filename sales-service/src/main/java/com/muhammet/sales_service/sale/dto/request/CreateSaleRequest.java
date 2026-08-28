package com.muhammet.sales_service.sale.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateSaleRequest(


        @NotNull
        UUID stockItemId,

        @NotNull
        @DecimalMin(
                value = "0.001"
        )
        BigDecimal quantity,

        @NotNull
        @DecimalMin(
                value = "0.00"
        )
        BigDecimal unitPrice,

        @NotNull
        Instant soldAt

) {
}