package com.muhammet.inventory_service.production.entity;

import com.muhammet.inventory_service.stock.entity.StockItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "production_batches",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_production_batches_operation_id",
                        columnNames = "operation_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductionBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * HTTP idempotency key.
     * Client'ın sağladığı UUID; aynı key ile ikinci çağrı yeni batch oluşturmaz.
     */
    @Column(name = "operation_id", nullable = false, updatable = false)
    private UUID operationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_batch_recipe"))
    private ProductionRecipe recipe;

    @Column(name = "output_quantity", nullable = false, precision = 19, scale = 3)
    private BigDecimal outputQuantity;

    // ---- Snapshot alanları (recipe sonradan değişse bile geçmiş korunur) ----

    @Column(name = "essence_quantity_consumed", nullable = false, precision = 19, scale = 3)
    private BigDecimal essenceQuantityConsumed;

    @Column(name = "bottle_quantity_consumed", nullable = false, precision = 19, scale = 3)
    private BigDecimal bottleQuantityConsumed;

    @Column(name = "packaging_quantity_consumed", nullable = false, precision = 19, scale = 3)
    private BigDecimal packagingQuantityConsumed;

    /** Essence StockItem UUID snapshot */
    @Column(name = "essence_stock_item_id", nullable = false)
    private UUID essenceStockItemId;

    /** Bottle StockItem UUID snapshot */
    @Column(name = "bottle_stock_item_id", nullable = false)
    private UUID bottleStockItemId;

    /** Packaging set StockItem UUID snapshot */
    @Column(name = "packaging_set_stock_item_id", nullable = false)
    private UUID packagingSetStockItemId;

    /** Output (finished product) StockItem UUID snapshot */
    @Column(name = "output_stock_item_id", nullable = false)
    private UUID outputStockItemId;

    @Column(name = "produced_by_user_id", nullable = false)
    private UUID producedByUserId;

    @Column(name = "produced_at", nullable = false, updatable = false)
    private Instant producedAt;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public static ProductionBatch create(
            UUID operationId,
            ProductionRecipe recipe,
            BigDecimal outputQuantity,
            BigDecimal essenceQuantityConsumed,
            BigDecimal bottleQuantityConsumed,
            BigDecimal packagingQuantityConsumed,
            UUID producedByUserId,
            String note
    ) {
        ProductionBatch b = new ProductionBatch();
        b.operationId = Objects.requireNonNull(operationId);
        b.recipe = Objects.requireNonNull(recipe);
        b.outputQuantity = Objects.requireNonNull(outputQuantity);
        b.essenceQuantityConsumed = Objects.requireNonNull(essenceQuantityConsumed);
        b.bottleQuantityConsumed = Objects.requireNonNull(bottleQuantityConsumed);
        b.packagingQuantityConsumed = Objects.requireNonNull(packagingQuantityConsumed);
        b.essenceStockItemId = recipe.getEssenceStockItem().getId();
        b.bottleStockItemId = recipe.getBottleStockItem().getId();
        b.packagingSetStockItemId = recipe.getPackagingSetStockItem().getId();
        b.outputStockItemId = recipe.getOutputStockItem().getId();
        b.producedByUserId = Objects.requireNonNull(producedByUserId);
        b.producedAt = Instant.now();
        b.note = (note != null && !note.isBlank()) ? note.trim() : null;
        return b;
    }
}

