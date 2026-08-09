package com.muhammet.inventory_service.stock.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
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
}
