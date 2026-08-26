package com.muhammet.inventory_service.stock.dto;

import com.muhammet.inventory_service.stock.enums.StockItemType;
import com.muhammet.inventory_service.stock.enums.StockUnit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockBalanceResponse(

        UUID stockBalanceId,

        UUID stockItemId,
        String name,
        String sku,
        StockItemType itemType,
        StockUnit unit,

        BigDecimal onHandQuantity,
        BigDecimal reservedQuantity,
        BigDecimal availableQuantity,

        boolean active,

        Instant updatedAt

) {
}