package com.muhammet.inventory_service.stock.dto;

public record InventorySummaryResponse(

        long totalStockItems,

        long activeStockItems,

        long inactiveStockItems,

        long outOfStockItems,

        long stockMovementCountToday

) {
}