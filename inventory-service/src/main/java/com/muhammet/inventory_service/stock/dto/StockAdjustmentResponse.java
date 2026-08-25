package com.muhammet.inventory_service.stock.dto;

import com.muhammet.inventory_service.stock.enums.StockAdjustmentReason;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockAdjustmentResponse(

        UUID stockMovementId,

        UUID stockItemId,

        BigDecimal previousOnHandQuantity,

        BigDecimal quantityChange,

        BigDecimal newOnHandQuantity,

        BigDecimal availableQuantity,

        StockAdjustmentReason reason,

        String note,

        Instant adjustedAt

) {
}