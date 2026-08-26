package com.muhammet.inventory_service.stock.dto;

import com.muhammet.inventory_service.stock.enums.StockMovementType;
import com.muhammet.inventory_service.stock.enums.StockUnit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse(

        UUID id,

        UUID stockItemId,
        String stockItemName,
        String sku,
        StockUnit unit,

        StockMovementType movementType,

        BigDecimal quantityChange,
        BigDecimal previousOnHandQuantity,
        BigDecimal newOnHandQuantity,

        String referenceType,
        String referenceId,

        String reasonCode,
        String note,

        Instant sourceOccurredAt,
        Instant createdAt

) {
}