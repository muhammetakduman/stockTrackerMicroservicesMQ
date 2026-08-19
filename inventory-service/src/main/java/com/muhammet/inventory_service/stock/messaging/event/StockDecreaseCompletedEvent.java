package com.muhammet.inventory_service.stock.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record StockDecreaseCompletedEvent(

        UUID eventId,

        String eventType,

        int eventVersion,

        UUID sourceEventId,

        Long saleId,

        UUID stockItemId,

        UUID stockMovementId,

        BigDecimal quantityDecreased,

        BigDecimal newOnHandQuantity,

        Instant occurredAt

) {

    public static final String EVENT_TYPE =
            "stock.decrease.completed";

    public static final int EVENT_VERSION = 1;

    public StockDecreaseCompletedEvent {

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
                saleId,
                "Sale ID cannot be null"
        );

        if (saleId <= 0) {
            throw new IllegalArgumentException(
                    "Sale ID must be greater than zero"
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
                quantityDecreased,
                "Quantity decreased cannot be null"
        );

        if (quantityDecreased.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Quantity decreased must be greater than zero"
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

    public static StockDecreaseCompletedEvent create(
            UUID sourceEventId,
            Long saleId,
            UUID stockItemId,
            UUID stockMovementId,
            BigDecimal quantityDecreased,
            BigDecimal newOnHandQuantity
    ) {
        return new StockDecreaseCompletedEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                EVENT_VERSION,
                sourceEventId,
                saleId,
                stockItemId,
                stockMovementId,
                quantityDecreased,
                newOnHandQuantity,
                Instant.now()
        );
    }
}