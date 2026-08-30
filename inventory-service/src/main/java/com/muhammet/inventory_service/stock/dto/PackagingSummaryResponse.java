package com.muhammet.inventory_service.stock.dto;

public record PackagingSummaryResponse(

        PackagingStockResponse bottle,

        PackagingStockResponse maleSet,

        PackagingStockResponse femaleSet,

        PackagingStockResponse unisexSet

) {
}