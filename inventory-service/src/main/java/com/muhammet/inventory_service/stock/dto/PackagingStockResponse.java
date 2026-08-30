package com.muhammet.inventory_service.stock.dto;

import com.muhammet.inventory_service.stock.enums.PackagingKind;

import java.math.BigDecimal;
import java.util.UUID;

public record PackagingStockResponse(

        PackagingKind packagingKind,

        boolean configured,

        UUID stockItemId,

        String name,

        String sku,

        BigDecimal onHandQuantity,

        BigDecimal reservedQuantity,

        BigDecimal availableQuantity

) {

    public static PackagingStockResponse notConfigured(
            PackagingKind packagingKind
    ) {
        return new PackagingStockResponse(
                packagingKind,
                false,
                null,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }
}