package com.muhammet.inventory_service.stock.dto;

import com.muhammet.inventory_service.stock.enums.PackagingKind;
import com.muhammet.inventory_service.stock.enums.StockItemType;
import com.muhammet.inventory_service.stock.enums.StockUnit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockItemResponse(
        UUID id,
        String name,
        String sku,
        String description,
        StockItemType itemType,
        StockUnit unit,
        PackagingKind packagingKind,
        BigDecimal onHandQuantity,
        BigDecimal reservedQuantity,
        BigDecimal availableQuantity,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
