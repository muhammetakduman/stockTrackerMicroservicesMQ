package com.muhammet.inventory_service.stock.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SaleCreatedEvent(

        UUID eventId,

        String eventType,

        int eventVersion,

        Long saleId,

        UUID sellerId,

        UUID stockItemId,

        BigDecimal quantity,

        Instant soldAt,

        Instant occurredAt

) {
}