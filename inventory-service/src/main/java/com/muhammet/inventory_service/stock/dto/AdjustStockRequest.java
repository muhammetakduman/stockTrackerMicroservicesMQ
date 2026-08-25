package com.muhammet.inventory_service.stock.dto;

import com.muhammet.inventory_service.stock.enums.StockAdjustmentReason;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AdjustStockRequest(

        @NotNull
        @DecimalMin(value = "0.000", inclusive = true)
        @Digits(integer = 16, fraction = 3)
        BigDecimal targetOnHandQuantity,

        @NotNull
        StockAdjustmentReason reason,

        @Size(max = 500)
        String note
) {
}