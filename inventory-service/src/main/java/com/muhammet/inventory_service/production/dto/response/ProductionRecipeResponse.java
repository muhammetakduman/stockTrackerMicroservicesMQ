package com.muhammet.inventory_service.production.dto.response;

import com.muhammet.inventory_service.production.entity.ProductionRecipe;
import com.muhammet.inventory_service.stock.enums.PackagingKind;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductionRecipeResponse(

        UUID id,

        String name,

        String description,


        UUID essenceStockItemId,
        String essenceStockItemName,


        UUID bottleStockItemId,
        String bottleStockItemName,


        UUID packagingSetStockItemId,
        String packagingSetStockItemName,
        PackagingKind packagingKind,


        UUID outputStockItemId,
        String outputStockItemName,


        BigDecimal essenceQuantityPerUnit,

        BigDecimal bottleQuantityPerUnit,

        BigDecimal packagingQuantityPerUnit,


        boolean active,

        Instant createdAt,

        Instant updatedAt

) {

    public static ProductionRecipeResponse from(
            ProductionRecipe recipe
    ) {

        return new ProductionRecipeResponse(

                recipe.getId(),

                recipe.getName(),

                recipe.getDescription(),


                recipe.getEssenceStockItem().getId(),
                recipe.getEssenceStockItem().getName(),


                recipe.getBottleStockItem().getId(),
                recipe.getBottleStockItem().getName(),


                recipe.getPackagingSetStockItem().getId(),
                recipe.getPackagingSetStockItem().getName(),
                recipe.getPackagingSetStockItem().getPackagingKind(),


                recipe.getOutputStockItem().getId(),
                recipe.getOutputStockItem().getName(),


                recipe.getEssenceQuantityPerUnit(),

                recipe.getBottleQuantityPerUnit(),

                recipe.getPackagingQuantityPerUnit(),


                recipe.isActive(),

                recipe.getCreatedAt(),

                recipe.getUpdatedAt()
        );
    }
}