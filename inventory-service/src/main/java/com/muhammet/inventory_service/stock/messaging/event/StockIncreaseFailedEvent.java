package com.muhammet.inventory_service.stock.messaging.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record StockIncreaseFailedEvent(

        UUID eventId,

        String eventType,

        int eventVersion,

        UUID sourceEventId,

        Long purchaseId,

        UUID stockItemId,

        String errorCode,

        String failureReason,

        Instant occurredAt

) {

    public static final String EVENT_TYPE =
            "stock.increase.failed";

    public static final int EVENT_VERSION = 1;

    public StockIncreaseFailedEvent {

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

    public static StockIncreaseFailedEvent create(
            UUID sourceEventId,
            Long purchaseId,
            UUID stockItemId,
            String errorCode,
            String failureReason
    ) {
        return new StockIncreaseFailedEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                EVENT_VERSION,
                sourceEventId,
                purchaseId,
                stockItemId,
                errorCode,
                failureReason,
                Instant.now()
        );
    }
}