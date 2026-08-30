package com.muhammet.inventory_service.stock.dto;

import com.muhammet.inventory_service.stock.enums.PackagingKind;
import com.muhammet.inventory_service.stock.enums.StockItemType;
import com.muhammet.inventory_service.stock.enums.StockUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateStockItemRequest(
        @NotBlank(message = "Stok kalemi adı boş olamaz")
        @Size(max=150, message = "Stok kalemi adı en fazla 150 karakter olabilir")
        String name,

        @NotBlank(message = "SKU boş olamaz")
        @Size(max = 50, message = "sku en fazla 50 karakter olabilir")
        String sku,

        @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir")
        String description,

        @NotNull(message = "Stok kalemi tipi belirtilmelidir")
        StockItemType itemType,

        @NotNull(message = "Stok birimi belirtilmelidir")
        StockUnit unit,

        /**
         * Yalnızca itemType = PACKAGING için zorunludur.
         * Diğer tipler için null olmalıdır.
         */
        PackagingKind packagingKind

){

}
