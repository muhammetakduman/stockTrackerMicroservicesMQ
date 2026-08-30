package com.muhammet.sales_service.sale.domain;
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
        name = "sales",
        indexes = {
                @Index(
                        name = "idx_sales_seller_id",
                        columnList = "seller_id"
                ),
                @Index(
                        name = "idx_sales_stock_item_id",
                        columnList = "stock_item_id"
                ),
                @Index(
                        name = "idx_sales_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_sales_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "seller_id",
            nullable = false,
            columnDefinition = "uuid"
    )
    private UUID sellerId;

    @Column(
            name = "stock_item_id",
            nullable = false
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
            scale = 2
    )
    private BigDecimal unitPrice;

    @Column(
            name = "total_price",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 40
    )
    private SaleStatus status;

    @Column(
            name = "failure_reason",
            length = 500
    )
    private String failureReason;

    @Column(
            name = "sold_at",
            nullable = false
    )
    private Instant soldAt;

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


    private Sale(
            UUID sellerId,
            UUID customerId,
            String customerNameSnapshot,
            UUID stockItemId,
            BigDecimal quantity,
            BigDecimal unitPrice,
            Instant soldAt,
            String note
    ) {

        validateSellerId(sellerId);

        this.stockItemId = Objects.requireNonNull(
                stockItemId,
                "Stock item ID cannot be null"
        );

        validatePositiveQuantity(quantity);

        validateUnitPrice(unitPrice);

        this.sellerId = sellerId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;

        this.totalPrice =
                unitPrice.multiply(quantity);

        this.soldAt = Objects.requireNonNull(
                soldAt,
                "Sold time cannot be null"
        );

        this.status =
                SaleStatus.PENDING_STOCK_UPDATE;
        this.customerId = Objects.requireNonNull(
                customerId,
                "Customer ID cannot be null"
        );

        if (customerNameSnapshot == null ||
                customerNameSnapshot.isBlank()) {

            throw new IllegalArgumentException(
                    "Customer name cannot be blank"
            );
        }

        this.customerNameSnapshot =
                customerNameSnapshot.trim();
    }


    public static Sale create(
            UUID sellerId,
            UUID customerId,
            String customerNameSnapshot,
            UUID stockItemId,
            BigDecimal quantity,
            BigDecimal unitPrice,
            Instant soldAt,
            String note
    ) {

        return new Sale(
                sellerId,
                customerId,
                customerNameSnapshot,
                stockItemId,
                quantity,
                unitPrice,
                soldAt,
                note
        );
    }

    public void markStockUpdateCompleted() {

        if (this.status ==
                SaleStatus.COMPLETED) {

            return;
        }

        if (this.status ==
                SaleStatus.FAILED) {

            throw new IllegalStateException(
                    "Failed sale cannot be completed"
            );
        }

        this.status =
                SaleStatus.COMPLETED;

        this.failureReason = null;
    }


    public void markStockUpdateFailed(
            String failureReason
    ) {

        if (this.status ==
                SaleStatus.FAILED) {

            return;
        }

        if (this.status ==
                SaleStatus.COMPLETED) {

            throw new IllegalStateException(
                    "Completed sale cannot be failed"
            );
        }

        if (failureReason == null ||
                failureReason.isBlank()) {

            throw new IllegalArgumentException(
                    "Failure reason cannot be blank"
            );
        }

        this.status =
                SaleStatus.FAILED;

        this.failureReason =
                failureReason.trim();
    }


    @PrePersist
    protected void onCreate() {

        Instant now =
                Instant.now();

        this.createdAt = now;
        this.updatedAt = now;
    }


    @PreUpdate
    protected void onUpdate() {

        this.updatedAt =
                Instant.now();
    }


    private static void validateSellerId(
            UUID sellerId
    ) {

        if (sellerId == null) {

            throw new IllegalArgumentException(
                    "Seller ID cannot be null"
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

        if (quantity.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }


    private static void validateUnitPrice(
            BigDecimal unitPrice
    ) {

        if (unitPrice == null) {

            throw new IllegalArgumentException(
                    "Unit price cannot be null"
            );
        }

        if (unitPrice.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new IllegalArgumentException(
                    "Unit price cannot be negative"
            );
        }
    }
    @Column(
            name = "customer_id"
    )
    private UUID customerId;

    @Column(
            name = "customer_name_snapshot",
            length = 150
    )
    private String customerNameSnapshot;

    @Column(
            name = "note",
            length = 1000
    )
    private String note;
}