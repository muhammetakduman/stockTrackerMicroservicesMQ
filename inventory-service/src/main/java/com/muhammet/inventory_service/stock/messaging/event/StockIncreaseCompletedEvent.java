package com.muhammet.inventory_service.stock.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record StockIncreaseCompletedEvent(

        UUID eventId,

        String eventType,

        int eventVersion,

        UUID sourceEventId,

        Long purchaseId,

        UUID stockItemId,

        UUID stockMovementId,

        BigDecimal quantityAdded,

        BigDecimal newOnHandQuantity,

        Instant occurredAt

) {

    public static final String EVENT_TYPE =
            "stock.increase.completed";

    public static final int EVENT_VERSION = 1;

    public StockIncreaseCompletedEvent {

        Objects.requireNonNull(
                eventId,
                "Event ID cannot be null"
        );

        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException(
                    "Event type cannot be blank"
            );
        }

        if (eventVersion <= 0) {
            throw new IllegalArgumentException(
                    "Event version must be greater than zero"
            );
        }

        Objects.requireNonNull(
                sourceEventId,
                "Source event ID cannot be null"
        );

        Objects.requireNonNull(
                purchaseId,
                "Purchase ID cannot be null"
        );

        if (purchaseId <= 0) {
            throw new IllegalArgumentException(
                    "Purchase ID must be greater than zero"
            );
        }

        Objects.requireNonNull(
                stockItemId,
                "Stock item ID cannot be null"
        );

        Objects.requireNonNull(
                stockMovementId,
                "Stock movement ID cannot be null"
        );

        Objects.requireNonNull(
                quantityAdded,
                "Quantity added cannot be null"
        );

        if (quantityAdded.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Quantity added must be greater than zero"
            );
        }

        Objects.requireNonNull(
                newOnHandQuantity,
                "New on-hand quantity cannot be null"
        );

        if (newOnHandQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "New on-hand quantity cannot be negative"
            );
        }

        Objects.requireNonNull(
                occurredAt,
                "Occurrence time cannot be null"
        );
    }

    public static StockIncreaseCompletedEvent create(
            UUID sourceEventId,
            Long purchaseId,
            UUID stockItemId,
            UUID stockMovementId,
            BigDecimal quantityAdded,
            BigDecimal newOnHandQuantity
    ) {
        return new StockIncreaseCompletedEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                EVENT_VERSION,
                sourceEventId,
                purchaseId,
                stockItemId,
                stockMovementId,
                quantityAdded,
                newOnHandQuantity,
                Instant.now()
        );
    }
}