package com.muhammet.purchase_service.purchase.messaging.event;

import com.muhammet.purchase_service.purchase.domain.Purchase;
import com.muhammet.purchase_service.purchase.domain.PurchaseStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PurchaseCreatedEvent(

        UUID eventId,

        String eventType,

        int eventVersion,

        Long purchaseId,

        UUID stockItemId,

        BigDecimal quantity,

        Instant purchasedAt,

        Instant occurredAt

) {

    public static final String TYPE = "purchase.created";
    public static final int VERSION = 1;

    public PurchaseCreatedEvent {
        Objects.requireNonNull(eventId, "Event ID cannot be null");
        Objects.requireNonNull(eventType, "Event type cannot be null");
        Objects.requireNonNull(purchaseId, "Purchase ID cannot be null");
        Objects.requireNonNull(stockItemId, "Stock item ID cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(purchasedAt, "Purchase date cannot be null");
        Objects.requireNonNull(occurredAt, "Event occurrence date cannot be null");

        if (!TYPE.equals(eventType)) {
            throw new IllegalArgumentException(
                    "Event type must be " + TYPE
            );
        }

        if (eventVersion < 1) {
            throw new IllegalArgumentException(
                    "Event version must be greater than zero"
            );
        }

        if (purchaseId <= 0) {
            throw new IllegalArgumentException(
                    "Purchase ID must be greater than zero"
            );
        }

        if (stockItemId == null) {
            throw new IllegalArgumentException(
                    "Stock item ID cannot be null"
            );
        }

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }

    public static PurchaseCreatedEvent from(Purchase purchase) {
        Objects.requireNonNull(
                purchase,
                "Purchase cannot be null"
        );

        if (purchase.getId() == null) {
            throw new IllegalStateException(
                    "Purchase must be saved before creating an event"
            );
        }

        if (purchase.getStatus()
                != PurchaseStatus.PENDING_STOCK_UPDATE) {
            throw new IllegalStateException(
                    "Purchase must be pending stock update"
            );
        }

        return new PurchaseCreatedEvent(
                UUID.randomUUID(),
                TYPE,
                VERSION,
                purchase.getId(),
                purchase.getStockItemId(),
                purchase.getQuantity(),
                purchase.getPurchasedAt(),
                Instant.now()
        );
    }
}