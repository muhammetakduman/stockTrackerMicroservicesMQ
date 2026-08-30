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
@Table(name = "production_recipes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductionRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "essence_stock_item_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_recipe_essence_item"))
    private StockItem essenceStockItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bottle_stock_item_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_recipe_bottle_item"))
    private StockItem bottleStockItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "packaging_set_stock_item_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_recipe_packaging_set_item"))
    private StockItem packagingSetStockItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "output_stock_item_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_recipe_output_item"))
    private StockItem outputStockItem;

    @Column(name = "essence_quantity_per_unit", nullable = false, precision = 19, scale = 3)
    private BigDecimal essenceQuantityPerUnit;

    @Column(name = "bottle_quantity_per_unit", nullable = false, precision = 19, scale = 3)
    private BigDecimal bottleQuantityPerUnit;

    @Column(name = "packaging_quantity_per_unit", nullable = false, precision = 19, scale = 3)
    private BigDecimal packagingQuantityPerUnit;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public static ProductionRecipe create(
            String name, String description,
            StockItem essenceStockItem, StockItem bottleStockItem,
            StockItem packagingSetStockItem, StockItem outputStockItem,
            BigDecimal essenceQuantityPerUnit,
            BigDecimal bottleQuantityPerUnit,
            BigDecimal packagingQuantityPerUnit) {
        ProductionRecipe r = new ProductionRecipe();
        r.name = Objects.requireNonNull(name).trim();
        r.description = (description != null && !description.isBlank()) ? description.trim() : null;
        r.essenceStockItem = Objects.requireNonNull(essenceStockItem);
        r.bottleStockItem = Objects.requireNonNull(bottleStockItem);
        r.packagingSetStockItem = Objects.requireNonNull(packagingSetStockItem);
        r.outputStockItem = Objects.requireNonNull(outputStockItem);
        r.essenceQuantityPerUnit = Objects.requireNonNull(essenceQuantityPerUnit);
        r.bottleQuantityPerUnit = Objects.requireNonNull(bottleQuantityPerUnit);
        r.packagingQuantityPerUnit = Objects.requireNonNull(packagingQuantityPerUnit);
        r.active = true;
        return r;
    }

    public void activate() { this.active = true; }

    public void deactivate() { this.active = false; }
}

