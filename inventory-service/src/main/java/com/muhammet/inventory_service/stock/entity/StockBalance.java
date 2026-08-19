package com.muhammet.inventory_service.stock.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "stock_balances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "stock_item_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_stock_balance_item"
            )
    )
    private StockItem stockItem;

    @Column(
            name = "on_hand_quantity",
            nullable = false,
            precision = 19,
            scale = 3
    )
    private BigDecimal onHandQuantity = BigDecimal.ZERO;

    @Column(
            name = "reserved_quantity",
            nullable = false,
            precision = 19,
            scale = 3
    )
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Version
    private Long version;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public StockBalance(StockItem stockItem) {
        this.stockItem = Objects.requireNonNull(
                stockItem,
                "Stock item cannot be null"
        );
        this.onHandQuantity = BigDecimal.ZERO;
        this.reservedQuantity = BigDecimal.ZERO;
    }

    public void increaseOnHandQuantity(BigDecimal quantity) {

        if (quantity == null) {
            throw new IllegalArgumentException(
                    "Quantity cannot be null"
            );
        }

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        this.onHandQuantity =
                this.onHandQuantity.add(quantity);
    }

    public void decreaseOnHandQuantity(
            BigDecimal quantity
    ) {
        Objects.requireNonNull(
                quantity,
                "Decrease quantity cannot be null"
        );

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Decrease quantity must be greater than zero"
            );
        }

        BigDecimal availableQuantity =
                getAvailableQuantity();

        if (availableQuantity.compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient available stock. " +
                            "available=" + availableQuantity +
                            ", requested=" + quantity
            );
        }

        this.onHandQuantity =
                this.onHandQuantity.subtract(quantity);
    }

    @Transient
    public BigDecimal getAvailableQuantity() {
        return onHandQuantity.subtract(reservedQuantity);
    }

    void assignStockItem(StockItem stockItem) {
        this.stockItem = Objects.requireNonNull(
                stockItem,
                "Stock item cannot be null"
        );
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    private void validatePositiveQuantity(BigDecimal quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException(
                    "Quantity cannot be null"
            );
        }

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }

}