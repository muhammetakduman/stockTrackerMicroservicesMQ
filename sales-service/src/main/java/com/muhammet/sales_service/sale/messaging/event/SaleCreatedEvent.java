package com.muhammet.sales_service.sale.messaging.event;
import com.muhammet.sales_service.sale.domain.Sale;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record SaleCreatedEvent(

        UUID eventId,

        String eventType,

        int eventVersion,

        Long saleId,

        Long sellerId,

        UUID stockItemId,

        BigDecimal quantity,

        Instant soldAt,

        Instant occurredAt

) {

    public static final String EVENT_TYPE =
            "sale.created";

    public static final int EVENT_VERSION = 1;


    public SaleCreatedEvent {

        Objects.requireNonNull(
                eventId,
                "Event ID cannot be null"
        );

        if (eventType == null ||
                eventType.isBlank()) {

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
                saleId,
                "Sale ID cannot be null"
        );

        if (saleId <= 0) {

            throw new IllegalArgumentException(
                    "Sale ID must be greater than zero"
            );
        }

        Objects.requireNonNull(
                sellerId,
                "Seller ID cannot be null"
        );

        if (sellerId <= 0) {

            throw new IllegalArgumentException(
                    "Seller ID must be greater than zero"
            );
        }

        Objects.requireNonNull(
                stockItemId,
                "Stock item ID cannot be null"
        );

        Objects.requireNonNull(
                quantity,
                "Quantity cannot be null"
        );

        if (quantity.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        Objects.requireNonNull(
                soldAt,
                "Sold time cannot be null"
        );

        Objects.requireNonNull(
                occurredAt,
                "Occurred at cannot be null"
        );
    }


    public static SaleCreatedEvent from(
            Sale sale
    ) {

        Objects.requireNonNull(
                sale,
                "Sale cannot be null"
        );

        if (sale.getId() == null) {

            throw new IllegalStateException(
                    "Sale must be persisted before event creation"
            );
        }

        return new SaleCreatedEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                EVENT_VERSION,
                sale.getId(),
                sale.getSellerId(),
                sale.getStockItemId(),
                sale.getQuantity(),
                sale.getSoldAt(),
                Instant.now()
        );
    }
}