package com.muhammet.inventory_service.stock.entity;

import com.muhammet.inventory_service.stock.enums.StockMovementType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "stock_movements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_stock_movements_source_event_id",
                        columnNames = "source_event_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_stock_movements_stock_item_id",
                        columnList = "stock_item_id"
                ),
                @Index(
                        name = "idx_stock_movements_movement_type",
                        columnList = "movement_type"
                ),
                @Index(
                        name = "idx_stock_movements_created_at",
                        columnList = "created_at"
                ),
                @Index(
                        name = "idx_stock_movements_reference",
                        columnList = "reference_type, reference_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "source_event_id",
            nullable = false,
            updatable = false
    )
    private UUID sourceEventId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "stock_item_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_stock_movement_item"
            )
    )
    private StockItem stockItem;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "movement_type",
            nullable = false,
            length = 40
    )
    private StockMovementType movementType;

    @Column(
            name = "quantity_change",
            nullable = false,
            precision = 19,
            scale = 3
    )
    private BigDecimal quantityChange;

    @Column(
            name = "previous_on_hand_quantity",
            nullable = false,
            precision = 19,
            scale = 3
    )
    private BigDecimal previousOnHandQuantity;

    @Column(
            name = "new_on_hand_quantity",
            nullable = false,
            precision = 19,
            scale = 3
    )
    private BigDecimal newOnHandQuantity;

    @Column(
            name = "reference_type",
            nullable = false,
            length = 50
    )
    private String referenceType;

    @Column(
            name = "reference_id",
            nullable = false,
            length = 100
    )
    private String referenceId;

    @Column(
            name = "source_occurred_at",
            nullable = false,
            updatable = false
    )
    private Instant sourceOccurredAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    private StockMovement(
            UUID sourceEventId,
            StockItem stockItem,
            StockMovementType movementType,
            BigDecimal quantityChange,
            BigDecimal previousOnHandQuantity,
            BigDecimal newOnHandQuantity,
            String referenceType,
            String referenceId,
            Instant sourceOccurredAt
    ) {
        this.sourceEventId = Objects.requireNonNull(
                sourceEventId,
                "Source event ID cannot be null"
        );

        this.stockItem = Objects.requireNonNull(
                stockItem,
                "Stock item cannot be null"
        );

        this.movementType = Objects.requireNonNull(
                movementType,
                "Movement type cannot be null"
        );

        validatePositiveQuantity(quantityChange);
        validateNonNegativeQuantity(
                previousOnHandQuantity,
                "Previous on-hand quantity"
        );
        validateNonNegativeQuantity(
                newOnHandQuantity,
                "New on-hand quantity"
        );
        validateReference(referenceType, referenceId);

        this.quantityChange = quantityChange;
        this.previousOnHandQuantity = previousOnHandQuantity;
        this.newOnHandQuantity = newOnHandQuantity;
        this.referenceType = referenceType.trim();
        this.referenceId = referenceId.trim();

        this.sourceOccurredAt = Objects.requireNonNull(
                sourceOccurredAt,
                "Source occurrence time cannot be null"
        );
    }

    public static StockMovement purchaseReceipt(
            UUID sourceEventId,
            StockItem stockItem,
            BigDecimal receivedQuantity,
            BigDecimal previousOnHandQuantity,
            BigDecimal newOnHandQuantity,
            Long purchaseId,
            Instant sourceOccurredAt
    ) {
        Objects.requireNonNull(
                purchaseId,
                "Purchase ID cannot be null"
        );

        if (purchaseId <= 0) {
            throw new IllegalArgumentException(
                    "Purchase ID must be greater than zero"
            );
        }

        validatePurchaseResult(
                receivedQuantity,
                previousOnHandQuantity,
                newOnHandQuantity
        );

        return new StockMovement(
                sourceEventId,
                stockItem,
                StockMovementType.PURCHASE_RECEIPT,
                receivedQuantity,
                previousOnHandQuantity,
                newOnHandQuantity,
                "PURCHASE",
                purchaseId.toString(),
                sourceOccurredAt
        );
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    private static void validatePurchaseResult(
            BigDecimal receivedQuantity,
            BigDecimal previousOnHandQuantity,
            BigDecimal newOnHandQuantity
    ) {
        validatePositiveQuantity(receivedQuantity);

        validateNonNegativeQuantity(
                previousOnHandQuantity,
                "Previous on-hand quantity"
        );

        validateNonNegativeQuantity(
                newOnHandQuantity,
                "New on-hand quantity"
        );

        BigDecimal expectedNewQuantity =
                previousOnHandQuantity.add(receivedQuantity);

        if (expectedNewQuantity.compareTo(newOnHandQuantity) != 0) {
            throw new IllegalArgumentException(
                    "New on-hand quantity must equal " +
                            "previous quantity plus received quantity"
            );
        }
    }

    private static void validatePositiveQuantity(
            BigDecimal quantity
    ) {
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

    private static void validateNonNegativeQuantity(
            BigDecimal quantity,
            String fieldName
    ) {
        if (quantity == null) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be null"
            );
        }

        if (quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be negative"
            );
        }
    }

    private static void validateReference(
            String referenceType,
            String referenceId
    ) {
        if (referenceType == null || referenceType.isBlank()) {
            throw new IllegalArgumentException(
                    "Reference type cannot be blank"
            );
        }

        if (referenceId == null || referenceId.isBlank()) {
            throw new IllegalArgumentException(
                    "Reference ID cannot be blank"
            );
        }
    }
}