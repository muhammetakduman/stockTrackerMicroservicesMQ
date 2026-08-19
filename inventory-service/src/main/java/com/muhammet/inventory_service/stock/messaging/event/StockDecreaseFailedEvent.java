package com.muhammet.inventory_service.stock.messaging.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record StockDecreaseFailedEvent(

        UUID eventId,

        String eventType,

        int eventVersion,

        UUID sourceEventId,

        Long saleId,

        UUID stockItemId,

        String errorCode,

        String failureReason,

        Instant occurredAt

) {

    public static final String EVENT_TYPE =
            "stock.decrease.failed";

    public static final int EVENT_VERSION = 1;

    public StockDecreaseFailedEvent {

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

        if (errorCode == null || errorCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Error code cannot be blank"
            );
        }

        if (failureReason == null ||
                failureReason.isBlank()) {

            throw new IllegalArgumentException(
                    "Failure reason cannot be blank"
            );
        }

        Objects.requireNonNull(
                occurredAt,
                "Occurrence time cannot be null"
        );
    }

    public static StockDecreaseFailedEvent create(
            UUID sourceEventId,
            Long saleId,
            UUID stockItemId,
            String errorCode,
            String failureReason
    ) {
        return new StockDecreaseFailedEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                EVENT_VERSION,
                sourceEventId,
                saleId,
                stockItemId,
                errorCode,
                failureReason,
                Instant.now()
        );
    }
}