package com.muhammet.inventory_service.stock.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateStockItemStatusRequest(

        @NotNull
        Boolean active

) {
}