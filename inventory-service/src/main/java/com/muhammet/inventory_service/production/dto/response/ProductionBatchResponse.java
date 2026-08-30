package com.muhammet.inventory_service.production.dto.response;

import com.muhammet.inventory_service.production.entity.ProductionBatch;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductionBatchResponse(

        UUID id,

        UUID operationId,

        UUID recipeId,

        String recipeName,

        BigDecimal outputQuantity,

        BigDecimal essenceQuantityConsumed,

        BigDecimal bottleQuantityConsumed,

        BigDecimal packagingQuantityConsumed,

        UUID essenceStockItemId,

        UUID bottleStockItemId,

        UUID packagingSetStockItemId,

        UUID outputStockItemId,

        UUID producedByUserId,

        Instant producedAt,

        String note,

        Instant createdAt

) {

    public static ProductionBatchResponse from(
            ProductionBatch batch
    ) {

        return new ProductionBatchResponse(

                batch.getId(),

                batch.getOperationId(),

                batch.getRecipe().getId(),

                batch.getRecipe().getName(),

                batch.getOutputQuantity(),

                batch.getEssenceQuantityConsumed(),

                batch.getBottleQuantityConsumed(),

                batch.getPackagingQuantityConsumed(),

                batch.getEssenceStockItemId(),

                batch.getBottleStockItemId(),

                batch.getPackagingSetStockItemId(),

                batch.getOutputStockItemId(),

                batch.getProducedByUserId(),

                batch.getProducedAt(),

                batch.getNote(),

                batch.getCreatedAt()
        );
    }
}