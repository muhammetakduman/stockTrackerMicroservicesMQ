package com.muhammet.purchase_service.purchase.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "purchases",
        indexes = {
                @Index(
                        name = "idx_purchases_stock_item_id",
                        columnList = "stock_item_id"
                ),
                @Index(
                        name = "idx_purchases_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_purchases_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "stock_item_id",
            nullable = false,
            columnDefinition = "uuid"
    )
    private UUID stockItemId;

    @Column(
            name = "quantity",
            nullable = false,
            precision = 19,
            scale = 3
    )
    private BigDecimal quantity;

    @Column(
            name = "unit_price",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal unitPrice;

    @Column(
            name = "supplier_name",
            nullable = false,
            length = 150
    )
    private String supplierName;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 40
    )
    private PurchaseStatus status;

    @Column(
            name = "purchased_at",
            nullable = false
    )
    private Instant purchasedAt;

    @Column(
            name = "failure_reason",
            length = 500
    )
    private String failureReason;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version;

    private Purchase(
            UUID stockItemId,
            BigDecimal quantity,
            BigDecimal unitPrice,
            String supplierName,
            Instant purchasedAt
    ) {
        validateStockItemId(stockItemId);
        validateQuantity(quantity);
        validateUnitPrice(unitPrice);
        validateSupplierName(supplierName);

        this.stockItemId = stockItemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.supplierName = supplierName.trim();
        this.purchasedAt = purchasedAt != null
                ? purchasedAt
                : Instant.now();

        this.status = PurchaseStatus.PENDING_STOCK_UPDATE;
    }

    public static Purchase create(
            UUID stockItemId,
            BigDecimal quantity,
            BigDecimal unitPrice,
            String supplierName,
            Instant purchasedAt
    ) {
        return new Purchase(
                stockItemId,
                quantity,
                unitPrice,
                supplierName,
                purchasedAt
        );
    }

    public void markCompleted() {
        requireStatus(PurchaseStatus.PENDING_STOCK_UPDATE);

        this.status = PurchaseStatus.COMPLETED;
        this.failureReason = null;
    }

    public void markFailed(String failureReason) {
        requireStatus(PurchaseStatus.PENDING_STOCK_UPDATE);

        if (failureReason == null || failureReason.isBlank()) {
            throw new IllegalArgumentException(
                    "Failure reason cannot be blank"
            );
        }

        this.status = PurchaseStatus.FAILED;
        this.failureReason = failureReason.trim();
    }

    public void cancelBeforeStockUpdate() {
        requireStatus(PurchaseStatus.PENDING_STOCK_UPDATE);

        this.status = PurchaseStatus.CANCELLED;
    }

    public void markCancelledAfterCompensation() {
        requireStatus(PurchaseStatus.COMPLETED);

        this.status = PurchaseStatus.CANCELLED;
    }

    @PrePersist
    private void prePersist() {
        Instant now = Instant.now();

        if (purchasedAt == null) {
            purchasedAt = now;
        }

        if (status == null) {
            status = PurchaseStatus.PENDING_STOCK_UPDATE;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }

    private void requireStatus(PurchaseStatus expectedStatus) {
        if (status != expectedStatus) {
            throw new IllegalStateException(
                    "Purchase status must be "
                            + expectedStatus
                            + ", but current status is "
                            + status
            );
        }
    }

    private static void validateStockItemId(UUID stockItemId) {
        if (stockItemId == null) {
            throw new IllegalArgumentException(
                    "Stock item ID cannot be null"
            );
        }
    }

    private static void validateQuantity(BigDecimal quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException(
                    "Purchase quantity cannot be null"
            );
        }

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Purchase quantity must be greater than zero"
            );
        }
    }

    private static void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null) {
            throw new IllegalArgumentException(
                    "Unit price cannot be null"
            );
        }

        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Unit price must be greater than zero"
            );
        }
    }

    private static void validateSupplierName(String supplierName) {
        if (supplierName == null || supplierName.isBlank()) {
            throw new IllegalArgumentException(
                    "Supplier name cannot be blank"
            );
        }

        if (supplierName.trim().length() > 150) {
            throw new IllegalArgumentException(
                    "Supplier name cannot exceed 150 characters"
            );
        }
    }
    public void markStockUpdateCompleted() {

        if (this.status == PurchaseStatus.COMPLETED) {
            return;
        }

        if (this.status != PurchaseStatus.PENDING_STOCK_UPDATE) {
            throw new IllegalStateException(
                    "Purchase cannot be completed from status: "
                            + this.status
            );
        }

        this.status = PurchaseStatus.COMPLETED;
        this.failureReason = null;
    }
    public void markStockUpdateFailed(
            String failureReason
    ) {
        if (this.status == PurchaseStatus.FAILED) {
            return;
        }

        if (this.status !=
                PurchaseStatus.PENDING_STOCK_UPDATE) {

            throw new IllegalStateException(
                    "Purchase cannot be marked as failed " +
                            "from status: " + this.status
            );
        }

        if (failureReason == null ||
                failureReason.isBlank()) {

            throw new IllegalArgumentException(
                    "Failure reason cannot be blank"
            );
        }

        this.status = PurchaseStatus.FAILED;
        this.failureReason = failureReason.trim();
    }
}